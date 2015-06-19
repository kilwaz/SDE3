package application.utils;

import application.Main;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
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
            path = path.replace("SDE.jar","");
            propertiesPath = URLDecoder.decode(path + "/../../", "UTF-8") + "SDE.xml";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static boolean readXML() {
        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the
            // XML file
            dom = db.parse(propertiesPath);

            Element doc = dom.getDocumentElement();

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
        Document dom;
        Element e = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            Element rootEle = dom.createElement("SDE");

            // create data elements and place them under root
            e = dom.createElement("DBUsername");
            e.appendChild(dom.createTextNode(AppParams.MYSQL_USERNAME));
            rootEle.appendChild(e);

            e = dom.createElement("DBPassword");
            e.appendChild(dom.createTextNode(AppParams.MYSQL_PASSWORD));
            rootEle.appendChild(e);

            e = dom.createElement("DBConnectionString");
            e.appendChild(dom.createTextNode(AppParams.MYSQL_CONNECTION));
            rootEle.appendChild(e);

            dom.appendChild(rootEle);

            FileOutputStream fos = null;

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                // send DOM to file
                fos = new FileOutputStream(propertiesPath);
                tr.transform(new DOMSource(dom), new StreamResult(fos));
            } catch (TransformerException | IOException ex) {
                System.out.println(ex.getMessage());
            } finally {
                if(fos!= null){
                    try {
                        fos.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (ParserConfigurationException ex) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + ex);
        }
    }
}
