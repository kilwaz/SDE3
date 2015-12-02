package application.utils;

import application.Main;
import application.error.Error;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class AppProperties {
    private static String propertiesPath = null;
    private static AppProperties instance = null;

    private static Logger log = Logger.getLogger(AppProperties.class);

    public AppProperties() {
        instance = this;

        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            path = path.replace("SDE.jar", "");
            propertiesPath = URLDecoder.decode(path + "/../../", "UTF-8") + "SDE.xml";
        } catch (UnsupportedEncodingException ex) {
            Error.APP_PROPERTIES_READ.record().create(ex);
        }
    }

    public static boolean readXML() {
        Document document;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the
            // XML file
            document = db.parse(propertiesPath);

            Element documentElement = document.getDocumentElement();

            AppParams.setRemoteDatabaseConnection(getTextValue(AppParams.getRemoteDatabaseConnection(), documentElement, "DBConnectionString"));
            AppParams.setRemoteDatabasePassword(getTextValue(AppParams.getRemoteDatabasePassword(), documentElement, "DBPassword"));
            AppParams.setRemoteDatabaseUsername(getTextValue(AppParams.getRemoteDatabaseUsername(), documentElement, "DBUsername"));
            AppParams.setLocalDatabase(Boolean.parseBoolean(getTextValue(AppParams.isLocalDatabase().toString(), documentElement, "LocalDatabase")));
            AppParams.setLocalDatabaseName(getTextValue(AppParams.getRemoteDatabaseUsername(), documentElement, "LocalDatabaseName"));
            return true;

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Error.APP_PROPERTIES_XML_PARSE.record().create(ex);
        }

        return false;
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

    public static void saveToXML() {
        Document document;
        Element e;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            document = db.newDocument();

            // create the root element
            Element rootEle = document.createElement("SDE");

            // create data elements and place them under root
            e = document.createElement("DBUsername");
            e.appendChild(document.createTextNode(AppParams.getRemoteDatabaseUsername()));
            rootEle.appendChild(e);

            e = document.createElement("DBPassword");
            e.appendChild(document.createTextNode(AppParams.getRemoteDatabasePassword()));
            rootEle.appendChild(e);

            e = document.createElement("DBConnectionString");
            e.appendChild(document.createTextNode(AppParams.getRemoteDatabaseConnection()));
            rootEle.appendChild(e);

            e = document.createElement("LocalDatabase");
            e.appendChild(document.createTextNode(AppParams.isLocalDatabase().toString()));
            rootEle.appendChild(e);

            e = document.createElement("LocalDatabaseName");
            e.appendChild(document.createTextNode(AppParams.getLocalDatabaseName()));
            rootEle.appendChild(e);

            document.appendChild(rootEle);

            XMLTransform.writeXMLToFile(document, propertiesPath);
        } catch (ParserConfigurationException ex) {
            Error.APP_PROPERTIES_SAVE_XML.record().create(ex);
        }
    }

    public AppProperties getInstance() {
        if (instance == null) {
            instance = new AppProperties();
        }
        return instance;
    }
}
