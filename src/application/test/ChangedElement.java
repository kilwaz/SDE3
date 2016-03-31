package application.test;

import application.error.Error;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChangedElement {
    private static Logger log = Logger.getLogger(ChangedElement.class);
    private Optional<Element> initialElement;
    private Optional<Element> finalElement;
    private String changeType;
    private String initialValue;
    private String finalValue;
    private String attributeName;

    public ChangedElement(Element initialElement, Element finalElement, String attributeName, String changeType) {
        this.initialElement = Optional.ofNullable(initialElement);
        this.finalElement = Optional.ofNullable(finalElement);
        this.changeType = changeType;
        this.attributeName = attributeName;
    }

    public ChangedElement(Element initialElement, String initialValue, Element finalElement, String finalValue, String changeType) {
        this.initialElement = Optional.ofNullable(initialElement);
        this.initialValue = initialValue;
        this.finalElement = Optional.ofNullable(finalElement);
        this.finalValue = finalValue;
        this.changeType = changeType;
    }

    public void printChange() {
        if ("select".equals(changeType)) {
            log.info(initialElement.get().attr("id") + " select value has changed '" + initialValue + "' -> '" + finalValue + "'");
        } else if ("attribute".equals(changeType)) {
            for (Attribute att : initialElement.get().attributes()) {
                if (!att.getValue().equals(finalElement.get().attr(att.getKey()))) {
                    log.info(initialElement.get().attr("id") + " has '" + att.getKey() + "' changed '" + att.getValue() + "' -> '" + finalElement.get().attr(att.getKey()) + "'");
                }
            }
        } else if ("text".equals(changeType)) {
            String elementReference = "";
            if (initialElement.get().attr("id") == null || initialElement.get().attr("id").isEmpty()) {
                elementReference = SDEUtils.generateXPath(initialElement.get());
            } else {
                elementReference = initialElement.get().attr("id");
            }
            if ("input".equals(initialElement.get().tag().getName()) && initialElement.get().parent() != null && finalElement.get().parent() != null) {
                log.info(elementReference + " has text changed '" + initialElement.get().parent().text() + "' -> '" + finalElement.get().parent().ownText() + "'");
            } else {
                log.info(elementReference + " has text changed '" + initialElement.get().text() + "' -> '" + finalElement.get().text() + "'");
            }
        } else if ("newElement".equals(changeType)) {
            log.info("New element added -> " + initialElement.get().outerHtml());
        }
    }

    public String getInitialRef() {
        if (initialElement.isPresent()) {
            return SDEUtils.generateXPath(initialElement.get());
        }
        return "";
    }

    public String getFinalRef() {
        return SDEUtils.generateXPath(finalElement.get());
    }

    public String getFinalAttr(String attr) {
        return finalElement.get().attr(attr);
    }

    public String getInitialAttr(String attr) {
        return initialElement.get().attr(attr);
    }

    public String getFinalValue() {
        if ("text".equals(changeType)) {
            if ("input".equals(finalElement.get().tag().getName()) && finalElement.get().parent() != null) {
                return finalElement.get().parent().ownText();
            } else {
                return finalElement.get().text();
            }
        } else if ("attribute".equals(changeType)) {
            return finalElement.get().attr(attributeName);
        }
        return "";
    }

    public String getInitialValue() {
        return getValue(initialElement.get());
    }

    private String getValue(Element element) {
        if ("text".equals(changeType)) {
            if ("input".equals(element.tag().getName()) && element.parent() != null) {
                return element.parent().text();
            } else {
                return element.text();
            }
        } else if ("attribute".equals(changeType)) {
            return element.attr(attributeName);
        }
        return "";
    }

    public String getChangeType() {
        return changeType;
    }

    public Boolean matched(ExpectedElement expectedElement) {
        List<Boolean> results = new ArrayList<>();
        // Create the correct size array of nulls
        for (int i = 0; i < 5; i++) {
            results.add(i, null);
        }

//        log.info("****");
//        log.info("Changed: " + this);
//        log.info("Expected: " + expectedElement);
//        log.info("****");

//        if (getInitialValue() == null) {
//            log.info("INIT VALUE IS NULL!");
//        } else {
//            log.info("INIT VALUE IS '" + getInitialValue() + "'");
//        }

        if (getInitialRef().equals(expectedElement.getElementReference())) {
//            log.info("Reference Matched: " + getInitialRef() + " matched " + expectedElement.getElementReference());
            results.add(0, true); // 0
        }
        if (getInitialValue().equals(expectedElement.getBefore())) {
//            log.info("Initial value Matched: " + getInitialValue() + " matched " + expectedElement.getBefore());
            results.add(1, true); // 1
        }
        if (getFinalValue().equals(expectedElement.getAfter())) {
//            log.info("Final value Matched: " + getFinalValue() + " matched " + expectedElement.getAfter());
            results.add(2, true); // 2
        }
        if (getChangeType().equals(expectedElement.getChangeType())) {
//            log.info("Change type Matched: " + getChangeType() + " matched " + expectedElement.getChangeType());
            results.add(3, true); // 3
        }
        if (getAttributeName() != null && getAttributeName().equals(expectedElement.getAttribute())) {
//            log.info("Attribute name Matched: " + getAttributeName() + " matched " + expectedElement.getAttribute());
            results.add(4, true); // 4
        }
        if (expectedElement.getIncreasedBy() != null) {
            String beforeStr = "";
            String afterStr = "";
            try {
                beforeStr = getInitialValue().replaceAll("[^\\d.]", ""); // Removes all non-numeric characters
                afterStr = getFinalValue().replaceAll("[^\\d.]", ""); // Removes all non-numeric characters
                Double beforeDouble = beforeStr.isEmpty() ? 0d : Double.parseDouble(beforeStr);
                Double afterDouble = afterStr.isEmpty() ? 0d : Double.parseDouble(afterStr);
//                log.info("Comparing numbers " + beforeDouble + " to " + afterDouble);
                if (afterDouble - beforeDouble == expectedElement.getIncreasedBy()) {
//                    log.info("Increased by Matched: " + getAttributeName() + " matched " + expectedElement.getIncreasedBy());
                    results.add(5, true); // 5
                }
            } catch (NumberFormatException ex) {
                Error.PARSE_DOUBLE_FAILED.record().additionalInformation(beforeStr).additionalInformation(afterStr).hideStackInLog().create(ex);
            }
        }

        // Here we check the matched results of only the criteria which was specified, this lets us match any number of criteria that is supplied
        // criteria tells us what has been specified as part of the match
        // results tells us what has been matched
        List<Boolean> criteria = expectedElement.getOrderedCriteriaExists();
        Boolean result = true;
        for (int i = 0; i < criteria.size() - 1; i++) {
            if (criteria.get(i) != null && criteria.get(i)) {
                if (results.get(i) == null) {
                    result = false;
                    break;
                } else if (!results.get(i)) {
                    result = false;
                    break;
                }
            }
        }

        // If something is matched from a previous test, we don't want to undo that
        if (!expectedElement.getMatched()) {
            expectedElement.matched(result);
        }
        return result;
    }

    public String getAttributeName() {
        if ("text".equals(changeType)) {
            return "text";
        } else {
            return attributeName;
        }
    }

    public String toString() {
        return "type: " + changeType + " attribute: " + attributeName + " initVal: " + getInitialValue() + " finalVal: " + getFinalValue();
    }
}
