package sde.application.data.xml;

import sde.application.node.objects.Switch;
import sde.application.utils.SDEUtils;
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
        Element switchElement = document.createElement("TestInput");

        Element targetElement = document.createElement("Target");
        targetElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(aSwitch.getTarget())));

        Element enabledElement = document.createElement("Enabled");
        enabledElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(aSwitch.isEnabled().toString())));

        switchElement.appendChild(targetElement);
        switchElement.appendChild(enabledElement);

        return switchElement;
    }
}
