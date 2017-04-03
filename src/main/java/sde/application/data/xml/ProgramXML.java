package sde.application.data.xml;

import sde.application.gui.Program;
import sde.application.error.Error;
import sde.application.node.design.DrawableNode;
import sde.application.utils.SDEUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ProgramXML implements XML {
    private Program program;

    public ProgramXML(Program program) {
        this.program = program;
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

            // Create the root element for the program
            Element programElement = document.createElement("Program");

            if (program != null) {
                // Create and append the program's name
                Element programName = document.createElement("ProgramName");
                programName.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(program.getName())));
                programElement.appendChild(programName);

                // Create and append the program's locked status
                Element lockedStatus = document.createElement("Locked");
                lockedStatus.appendChild(document.createTextNode(program.getLocked() ? "Y" : "N"));
                programElement.appendChild(lockedStatus);

                // Create the element which will hold all of the node information
                Element nodesElement = document.createElement("Nodes");

                // Loops through savable attributes
                for (DrawableNode node : program.getFlowController().getNodes()) {
                    DrawableNodeXML drawableNodeXML = new DrawableNodeXML(node);
                    nodesElement.appendChild(drawableNodeXML.getXMLRepresentation(document));
                }

                programElement.appendChild(nodesElement);
            }

            document.appendChild(programElement);

            return document;
        } catch (ParserConfigurationException ex) {
            Error.NODE_XML_REPRESENTATION.record().create(ex);
        }

        return null;
    }

    @Override
    public Element getXMLRepresentation(Document document) {
        return null;
    }
}
