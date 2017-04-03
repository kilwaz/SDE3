package sde.application.data.xml;

import sde.application.data.NodeColour;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sde.application.error.Error;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

public class NodeColoursXML implements XML {
    private List<NodeColour> nodeColours;

    public NodeColoursXML(List<NodeColour> nodeColours) {
        this.nodeColours = nodeColours;
    }

    public Document getXMLRepresentation() {
        Document document;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            document = db.newDocument();

            Element nodeColoursElement = document.createElement("NodeColours");

            if (nodeColours != null) {
                for (NodeColour nodeColour : nodeColours) {
                    NodeColourXML nodeColourXML = new NodeColourXML(nodeColour);
                    nodeColoursElement.appendChild(nodeColourXML.getXMLRepresentation(document));
                }
            }

            document.appendChild(nodeColoursElement);

            return document;
        } catch (ParserConfigurationException ex) {
            Error.NODE_XML_REPRESENTATION.record().create(ex);
        }

        return null;
    }

    public Element getXMLRepresentation(Document document) {
        return null;
    }
}
