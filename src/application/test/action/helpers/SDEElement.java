package application.test.action.helpers;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

public class SDEElement {
    private static Logger log = Logger.getLogger(SDEElement.class);
    private Element elementJSoup = null;

    public SDEElement(Element elementJSoup) {
        this.elementJSoup = elementJSoup;
    }

    public SDEValue getText() {
        if (elementJSoup != null) {
            if ("input".equals(elementJSoup.tagName())) {
                return new SDEValue(elementJSoup.attr("value")); // Value attribute should always have the most up to date value
            } else {
//                log.info("************** TEXT = " + elementJSoup.text());
                return new SDEValue(elementJSoup.text());
            }
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
