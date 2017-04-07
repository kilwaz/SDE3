package sde.application.data.xml;

import sde.application.node.objects.Trigger;
import sde.application.utils.SDEUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TriggerXML implements XML {
    Trigger trigger;

    public TriggerXML(Trigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public Document getXMLRepresentation() {
        return null;
    }

    @Override
    public Element getXMLRepresentation(Document document) {
        // Create a new element to save all inputs inside
        Element triggerElement = document.createElement("TestInput");

        Element watchElement = document.createElement("Watch");
        watchElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(trigger.getWatch())));

        Element whenElement = document.createElement("When");
        whenElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(trigger.getWhen())));

        Element thenElement = document.createElement("Then");
        thenElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(trigger.getThen())));

        triggerElement.appendChild(watchElement);
        triggerElement.appendChild(whenElement);
        triggerElement.appendChild(thenElement);

        return triggerElement;
    }
}
