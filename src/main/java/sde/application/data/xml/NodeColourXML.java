package sde.application.data.xml;

import sde.application.data.NodeColour;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sde.application.error.Error;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class NodeColourXML implements XML {
    private NodeColour nodeColour;

    public NodeColourXML(NodeColour nodeColour) {
        this.nodeColour = nodeColour;
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

            if (nodeColour != null) {
                nodeColoursElement.appendChild(getXMLRepresentation(document));
            }

            document.appendChild(nodeColoursElement);

            return document;
        } catch (ParserConfigurationException ex) {
            Error.NODE_XML_REPRESENTATION.record().create(ex);
        }

        return null;
    }

    public Element getXMLRepresentation(Document document) {
        // Create the root element
        Element nodeColourElement = document.createElement(nodeColour.getClass().getSimpleName());

        Element className = document.createElement("Red");
        className.appendChild(document.createTextNode(nodeColour.getRed().toString()));

        Element variableName = document.createElement("Green");
        variableName.appendChild(document.createTextNode(nodeColour.getGreen().toString()));

        Element variableValue = document.createElement("Blue");
        variableValue.appendChild(document.createTextNode(nodeColour.getBlue().toString()));

        Element nodeTypeValue = document.createElement("NodeType");
        nodeTypeValue.appendChild(document.createTextNode(nodeColour.getNodeType()));

        nodeColourElement.appendChild(variableName);
        nodeColourElement.appendChild(className);
        nodeColourElement.appendChild(variableValue);
        nodeColourElement.appendChild(nodeTypeValue);

        return nodeColourElement;
    }
}
