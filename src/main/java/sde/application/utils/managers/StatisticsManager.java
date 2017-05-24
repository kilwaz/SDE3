package sde.application.utils.managers;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sde.application.Main;
import sde.application.error.Error;
import sde.application.utils.StatisticStore;
import sde.application.utils.XMLTransform;
import sde.application.utils.timers.AppUpTimeJob;
import sde.application.utils.timers.SaveStatisticsJob;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class StatisticsManager {
    private static StatisticsManager instance;
    private static String statisticsPath = null;
    private StatisticStore totalStatisticStore;
    private StatisticStore sessionStatisticStore;

    public StatisticsManager() {
        instance = this;

        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            path = path.replace("SDE.jar", "");
            statisticsPath = URLDecoder.decode(path + "/../../", "UTF-8") + "SDE-Statistics.xml";
        } catch (UnsupportedEncodingException ex) {
            Error.APP_STATISTICS_READ.record().create(ex);
        }

        totalStatisticStore = new StatisticStore();
        sessionStatisticStore = new StatisticStore();

        if (!readXML()) {
            saveToXML();
        }

        // Creates the timers we need
        createSaveStatisticsJob();
        createUpTimeJob();
    }

    public static StatisticsManager getInstance() {
        if (instance == null) {
            instance = new StatisticsManager();
        }
        return instance;
    }

    private static String getTextValue(String defaultValue, Element documentElement, String tagName) {
        String value = defaultValue;
        NodeList nl;
        nl = documentElement.getElementsByTagName(tagName);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }

    private void createUpTimeJob() {
        // Keep uptime
        JobDetail statisticsJob = JobBuilder.newJob(AppUpTimeJob.class).build();
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();

        simpleScheduleBuilder.repeatForever().withIntervalInSeconds(1);

        JobManager.getInstance().scheduleJob(statisticsJob, triggerBuilder.withSchedule(simpleScheduleBuilder).build());
        triggerBuilder.startNow();
    }

    private void createSaveStatisticsJob() {
        // Save the statistics every 5 minutes
        JobDetail statisticsJob = JobBuilder.newJob(SaveStatisticsJob.class).build();
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();

        simpleScheduleBuilder.repeatForever().withIntervalInMinutes(5);

        JobManager.getInstance().scheduleJob(statisticsJob, triggerBuilder.withSchedule(simpleScheduleBuilder).build());
        triggerBuilder.startNow();
    }

    public void saveStatistics() {
        saveToXML();
    }

    private boolean readXML() {
        Document document;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the
            // XML file
            document = db.parse(statisticsPath);

            Element documentElement = document.getDocumentElement();

            totalStatisticStore.totalRequestsProperty().set(Long.parseLong(getTextValue("0", documentElement, "TotalRequests")));
            totalStatisticStore.upTimeProperty().set(Long.parseLong(getTextValue("0", documentElement, "TotalUpTime")));
            totalStatisticStore.requestSizeProperty().set(Long.parseLong(getTextValue("0", documentElement, "TotalRequestSize")));
            totalStatisticStore.applicationStartsProperty().set(Integer.parseInt(getTextValue("0", documentElement, "TotalApplicationStarts")));
            totalStatisticStore.responseSizeProperty().set(Long.parseLong(getTextValue("0", documentElement, "TotalResponseSize")));
            totalStatisticStore.commandsProperty().set(Long.parseLong(getTextValue("0", documentElement, "TotalCommands")));
            totalStatisticStore.programsStartedProperty().set(Long.parseLong(getTextValue("0", documentElement, "TotalProgramStarts")));

            // Formats the current sizes
            totalStatisticStore.addRequestSize(0);
            totalStatisticStore.addResponseSize(0);

            sessionStatisticStore.totalRequestsProperty().set(0); // Only relates to current session
            sessionStatisticStore.upTimeProperty().set(0); // Only relates to current session
            sessionStatisticStore.requestSizeProperty().set(0); // Only relates to current session

            return true;
        } catch (ParserConfigurationException | SAXException | IOException | NumberFormatException ex) {
            Error.APP_PROPERTIES_XML_PARSE.record().create(ex);
        }

        return false;
    }

    public StatisticStore getTotalStatisticStore() {
        return totalStatisticStore;
    }

    public StatisticStore getSessionStatisticStore() {
        return sessionStatisticStore;
    }

    private void saveToXML() {
        Document document;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            document = db.newDocument();

            // create the root element
            Element rootEle = document.createElement("Statistics");

            // create data elements and place them under root
            rootEle.appendChild(buildSaveElement("TotalRequests", totalStatisticStore.totalRequestsProperty().getValue().toString(), document));
            rootEle.appendChild(buildSaveElement("TotalUpTime", totalStatisticStore.upTimeProperty().getValue().toString(), document));
            rootEle.appendChild(buildSaveElement("TotalRequestSize", totalStatisticStore.requestSizeProperty().getValue().toString(), document));
            rootEle.appendChild(buildSaveElement("TotalResponseSize", totalStatisticStore.responseSizeProperty().getValue().toString(), document));
            rootEle.appendChild(buildSaveElement("TotalApplicationStarts", totalStatisticStore.applicationStartsProperty().getValue().toString(), document));
            rootEle.appendChild(buildSaveElement("TotalCommands", totalStatisticStore.commandsProperty().getValue().toString(), document));
            rootEle.appendChild(buildSaveElement("TotalProgramStarts", totalStatisticStore.programsStartedProperty().getValue().toString(), document));

            document.appendChild(rootEle);

            XMLTransform.writeXMLToFile(document, statisticsPath);
        } catch (ParserConfigurationException ex) {
            Error.APP_STATISTICS_SAVE_XML.record().create(ex);
        }
    }

    private void saveSQLiteStatistics() {

    }

    private void createSQLiteStatistics() {

    }

    private Element buildSaveElement(String reference, String text, Document document) {
        Element e = document.createElement(reference);
        e.appendChild(document.createTextNode(text));

        return e;
    }
}
