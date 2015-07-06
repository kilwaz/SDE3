package application.test;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

public class ChangedElement {
    private Element initialElement;
    private Element finalElement;
    private String changeType;
    private String initialValue;
    private String finalValue;

    private static Logger log = Logger.getLogger(ChangedElement.class);

    public ChangedElement(Element initialElement, String initialValue, Element finalElement, String finalValue, String changeType) {
        this.initialElement = initialElement;
        this.initialValue = initialValue;
        this.finalElement = finalElement;
        this.finalValue = finalValue;
        this.changeType = changeType;
    }

    public void printChange() {
        if ("select".equals(changeType)) {
            log.info(initialElement.attr("id") + " select value has changed '" + initialValue + "' -> '" + finalValue + "'");
        } else if ("attribute".equals(changeType)) {
            for (Attribute att : initialElement.attributes()) {
                if (!att.getValue().equals(finalElement.attr(att.getKey()))) {
                    log.info(initialElement.attr("id") + " has '" + att.getKey() + "' changed '" + att.getValue() + "' -> '" + finalElement.attr(att.getKey()) + "'");
                }
            }
        } else if ("text".equals(changeType)) {
            log.info(initialElement.attr("id") + " has text changed '" + initialElement.text() + "' -> '" + initialElement.text() + "'");
        } else if ("newElement".equals(changeType)) {
            log.info("New element added -> " + initialElement.outerHtml());
        }
    }
}
