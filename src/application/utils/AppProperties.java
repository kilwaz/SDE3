package application.utils;

import application.Main;
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

    public AppProperties() {
        instance = this;

        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            path = path.replace("SDE.jar", "");
            propertiesPath = URLDecoder.decode(path + "/../../", "UTF-8") + "SDE.xml";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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

            Element doc = document.getDocumentElement();

            AppParams.MYSQL_CONNECTION = getTextValue(AppParams.MYSQL_CONNECTION, doc, "DBConnectionString");
            AppParams.MYSQL_PASSWORD = getTextValue(AppParams.MYSQL_PASSWORD, doc, "DBPassword");
            AppParams.MYSQL_USERNAME = getTextValue(AppParams.MYSQL_USERNAME, doc, "DBUsername");
            return true;

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            System.out.println(ex.getMessage());
        }

        return false;
    }

    private static String getTextValue(String def, Element doc, String tag) {
        String value = def;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
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
            e.appendChild(document.createTextNode(AppParams.MYSQL_USERNAME));
            rootEle.appendChild(e);

            e = document.createElement("DBPassword");
            e.appendChild(document.createTextNode(AppParams.MYSQL_PASSWORD));
            rootEle.appendChild(e);

            e = document.createElement("DBConnectionString");
            e.appendChild(document.createTextNode(AppParams.MYSQL_CONNECTION));
            rootEle.appendChild(e);

            document.appendChild(rootEle);

            XMLTransform.writeXMLToFile(document, propertiesPath);
        } catch (ParserConfigurationException ex) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + ex);
        }
    }
}
