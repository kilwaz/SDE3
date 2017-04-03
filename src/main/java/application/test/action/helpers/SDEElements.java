package application.test.action.helpers;

import org.apache.commons.collections.IteratorUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class SDEElements {
    private static Logger log = Logger.getLogger(SDEElements.class);

    private Elements elements;

    public SDEElements(Elements elements) {
        this.elements = elements;
    }

    public SDEElements select(String selection) {
        return new SDEElements(elements.select(selection));
    }

    public List<SDEElement> list() {
        List<SDEElement> listedElements = new ArrayList<>();
        for (Object element : IteratorUtils.toList(elements.iterator())) {
            if (element instanceof Element) {
                Element castElement = (Element) element;
                listedElements.add(new SDEElement(castElement));
            }
        }
        return listedElements;
    }
}
