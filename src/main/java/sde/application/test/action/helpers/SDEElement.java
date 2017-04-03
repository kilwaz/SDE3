package sde.application.test.action.helpers;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SDEElement {
    private static Logger log = Logger.getLogger(SDEElement.class);
    private Element elementJSoup = null;

    public SDEElement(Element elementJSoup) {
        this.elementJSoup = elementJSoup;
    }

    public Element getElement() {
        return elementJSoup;
    }

    public SDEValue getText() {
        if (elementJSoup != null) {
            if ("input".equals(elementJSoup.tagName())) {
                return new SDEValue(elementJSoup.attr("value")); // Value attribute should always have the most up to date value
            } else if ("select".equals(elementJSoup.tagName())) {
                Elements selectedElement = elementJSoup.select("option[selected]");
                if (selectedElement.size() == 1) {
                    return new SDEValue(selectedElement.first().text());
                }
            } else {
//                log.info("************** TEXT = " + elementJSoup.text());
                return new SDEValue(elementJSoup.text());
            }
        }

        return new SDEValue("");
    }

    public SDEValue getContent() {
        if (elementJSoup != null) {
            return new SDEValue(elementJSoup.html());
        }

        return new SDEValue("");
    }

    public SDEValue getAttribute(String attribute) {
        if (elementJSoup != null) {
            return new SDEValue(elementJSoup.attr(attribute));
        }

        return new SDEValue("");
    }
}
