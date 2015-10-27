package application.utils;

import application.error.Error;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
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
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class XMLTransform {
    private static Logger log = Logger.getLogger(XMLTransform.class);

    public static void writeXMLToFile(Document document, String file) {
        FileOutputStream fos = null;

        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            // Send DOM to file
            fos = new FileOutputStream(file);
            tr.transform(new DOMSource(document), new StreamResult(fos));
        } catch (TransformerException | IOException ex) {
            Error.XML_WRITE_TO_FILE.record().create(ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    Error.CLOSE_FILE_STREAM.record().create(ex);
                }
            }
        }
    }

    public static String writeXMLToString(Document document) {
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Send DOM to String Writer
            StringWriter stringWriter = new StringWriter();
            tr.transform(new DOMSource(document), new StreamResult(stringWriter));

            return stringWriter.toString();
        } catch (TransformerException ex) {
            Error.XML_WRITE_XML_TO_STRING.record().create(ex);
        }

        return null;
    }

    public static Document writeStringToXML(String source) {
        Document document = null;
        try {
            // Make an  instance of the DocumentBuilderFactory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the
            // XML file
            document = db.parse(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Error.XML_WRITE_TO_STRING.record().create(ex);
        }
        return document;
    }
}
