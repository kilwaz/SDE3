package sde.application.test.action.helpers;

public class IfTracker {
    private Boolean isSkippingIf = false;
    private String ifReference = "";

    public Boolean isSkippingIf() {
        return isSkippingIf;
    }

    public void setIsSkippingIf(Boolean isSkippingIf) {
        this.isSkippingIf = isSkippingIf;
        this.ifReference = "";
    }

    public String getIfReference() {
        return ifReference;
    }

    public void setIfReference(String ifReference) {
        this.ifReference = ifReference;
    }
}
