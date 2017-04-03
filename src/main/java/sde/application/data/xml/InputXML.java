package sde.application.data.xml;

import sde.application.node.objects.Input;
import sde.application.utils.SDEUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class InputXML implements XML {
    Input input;

    public InputXML(Input input) {
        this.input = input;
    }

    @Override
    public Document getXMLRepresentation() {
        return null;
    }

    @Override
    public Element getXMLRepresentation(Document document) {
        Element inputElement = document.createElement("TestInput");

        Element variableNameElement = document.createElement("VariableName");
        variableNameElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(input.getVariableName())));

        Element variableValueElement = document.createElement("VariableValue");
        variableValueElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(input.getVariableValue())));

        inputElement.appendChild(variableNameElement);
        inputElement.appendChild(variableValueElement);

        return inputElement;
    }
}
