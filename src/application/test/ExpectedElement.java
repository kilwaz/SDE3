package application.test;

import org.apache.log4j.Logger;

public class ExpectedElement {
    private String elementReference = "";
    private String changeType = "";
    private String attribute = "";
    private String before = "";
    private String after = "";

    private static Logger log = Logger.getLogger(ExpectedElement.class);

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

    public ExpectedElement id(String id) {
        this.elementReference = "//*[@id=\"" + id + "\"]";
        return this;
    }

    public ExpectedElement after(String after) {
        this.after = after;
        return this;
    }
}
