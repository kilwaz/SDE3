package application.test;

import application.error.Error;
import org.apache.log4j.Logger;

public class ExpectedElement {
    private static Logger log = Logger.getLogger(ExpectedElement.class);
    private String elementReference = "";
    private String changeType = "";
    private String attribute = "";
    private String before = "";
    private String after = "";
    private Double increasedBy = null;
    private Boolean matched = false;
    private ChangedElement matchedElement = null;

    public static ExpectedElement define() {
        return new ExpectedElement();
    }

    public ExpectedElement before(String before) {
        this.before = before;
        return this;
    }

    public ExpectedElement attribute(String attribute) {
        this.attribute = attribute;
        return this;
    }

    public ExpectedElement type(String changeType) {
        this.changeType = changeType;
        return this;
    }

    public ExpectedElement reference(String reference) {
        this.elementReference = reference;
        return this;
    }

    public ExpectedElement id(String id) {
        this.elementReference = "//*[@id=\"" + id + "\"]";
        return this;
    }

    public ExpectedElement after(String after) {
        this.after = after;
        return this;
    }

    public ExpectedElement increasedBy(Double increasedBy) {
        this.increasedBy = increasedBy;
        return this;
    }

    public ExpectedElement increasedBy(String increasedBy) {
        try {
            this.increasedBy = Double.parseDouble(increasedBy.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException ex) {
            //  Guess we don't really care about this
            Error.PARSE_DOUBLE_FAILED.record().create(ex);
            this.increasedBy = 0d;
        }

        return this;
    }

    public ExpectedElement matched(Boolean result) {
        this.matched = result;
        return this;
    }

    public ExpectedElement changedElement(ChangedElement matchedElement) {
        this.matchedElement = matchedElement;
        return this;
    }

    public String getAfter() {
        return after;
    }

    public String getBefore() {
        return before;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getChangeType() {
        return changeType;
    }

    public String getElementReference() {
        return elementReference;
    }

    public Double getIncreasedBy() {
        return increasedBy;
    }

    public Boolean getMatched() {
        return matched;
    }

    public ChangedElement getMatchedElement() {
        return matchedElement;
    }
}
