package application.data.xml;

import application.node.objects.Switch;
import application.utils.SDEUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SwitchXML implements XML {
    Switch aSwitch;

    public SwitchXML(Switch aSwitch) {
        this.aSwitch = aSwitch;
    }

    @Override
    public Document getXMLRepresentation() {
        return null;
    }

    @Override
    public Element getXMLRepresentation(Document document) {
        Element switchElement = document.createElement("Input");

        Element targetElement = document.createElement("Target");
        targetElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(aSwitch.getTarget())));

        Element enabledElement = document.createElement("Enabled");
        enabledElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(aSwitch.isEnabled().toString())));

        switchElement.appendChild(targetElement);
        switchElement.appendChild(enabledElement);

        return switchElement;
    }
}
