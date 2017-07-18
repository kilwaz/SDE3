package sde.application.data.xml;

import sde.application.data.SavableAttribute;
import sde.application.error.Error;
import sde.application.node.design.DrawableNode;
import sde.application.node.implementations.DataTableNode;
import sde.application.node.implementations.InputNode;
import sde.application.node.implementations.SwitchNode;
import sde.application.node.implementations.TriggerNode;
import sde.application.node.objects.Input;
import sde.application.node.objects.Switch;
import sde.application.node.objects.Trigger;
import sde.application.node.objects.datatable.DataTableRow;
import sde.application.utils.SDEUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DrawableNodeXML implements XML {
    private DrawableNode drawableNode;

    public DrawableNodeXML(DrawableNode drawableNode) {
        this.drawableNode = drawableNode;
    }

    public Document getXMLRepresentation() {
        Document document;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to keystore an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            document = db.newDocument();

            if (drawableNode != null) {
                document.appendChild(getXMLRepresentation(document));
            }

            return document;
        } catch (ParserConfigurationException ex) {
            Error.NODE_XML_REPRESENTATION.record().create(ex);
        }

        return null;
    }

    public Element getXMLRepresentation(Document document) {
        // Create the root element
        Element nodeElement = document.createElement(drawableNode.getClass().getSimpleName());

        // Record if this node is the start node of the program
        DrawableNode startNode = drawableNode.getProgram().getFlowController().getStartNode();
        if (startNode != null && startNode.equals(drawableNode)) {
            Element startNodeElement = document.createElement("IsStartNode");
            startNodeElement.appendChild(document.createTextNode("Yes"));
            nodeElement.appendChild(startNodeElement);
        }

        // Loops through savable attributes
        for (SavableAttribute savableAttribute : drawableNode.getDataToSave()) {
            Element elementNode = document.createElement("Variable");

            Element className = document.createElement("ClassName");
            className.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(savableAttribute.getClassName())));

            Element variableName = document.createElement("VariableName");
            variableName.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(savableAttribute.getVariableName())));

            Element variableValue = document.createElement("VariableValue");
            variableValue.appendChild(document.createCDATASection(SDEUtils.escapeXMLCData(savableAttribute.getVariable().toString())));

            elementNode.appendChild(variableName);
            elementNode.appendChild(className);
            elementNode.appendChild(variableValue);

            nodeElement.appendChild(elementNode);
        }

        if (drawableNode != null) {
            if (drawableNode instanceof TriggerNode) {
                TriggerNode triggerNode = (TriggerNode) drawableNode;
                Element triggersElements = document.createElement("Triggers");
                for (Trigger trigger : triggerNode.getTriggers()) {
                    TriggerXML triggerXML = new TriggerXML(trigger);
                    triggersElements.appendChild(triggerXML.getXMLRepresentation(document));
                    nodeElement.appendChild(triggersElements);
                }
            } else if (drawableNode instanceof DataTableNode) {
                DataTableNode dataTableNode = (DataTableNode) drawableNode;
                Element dataTableDataElement = document.createElement("DataTableData");
                for (DataTableRow dataTableRow : dataTableNode.getDataTableRows()) {
                    DataTableRowXML dataTableRowXML = new DataTableRowXML(dataTableRow);
                    dataTableDataElement.appendChild(dataTableRowXML.getXMLRepresentation(document));
                    nodeElement.appendChild(dataTableDataElement);
                }
            } else if (drawableNode instanceof InputNode) {
                InputNode inputNode = (InputNode) drawableNode;
                Element inputElement = document.createElement("TestInputs");
                for (Input input : inputNode.getInputs()) {
                    InputXML inputXML = new InputXML(input);
                    inputElement.appendChild(inputXML.getXMLRepresentation(document));
                    nodeElement.appendChild(inputElement);
                }
            } else if (drawableNode instanceof SwitchNode) {
                SwitchNode switchNode = (SwitchNode) drawableNode;
                Element switchElements = document.createElement("Switches");
                for (Switch aSwitch : switchNode.getSwitches()) {
                    SwitchXML switchXML = new SwitchXML(aSwitch);
                    switchElements.appendChild(switchXML.getXMLRepresentation(document));
                    nodeElement.appendChild(switchElements);
                }
            }
        }

        return nodeElement;
    }
}
