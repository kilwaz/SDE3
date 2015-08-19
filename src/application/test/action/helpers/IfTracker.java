package application.test.action.helpers;

public class IfTracker {
    private static Boolean isSkippingIf = false;
    private static String ifReference = "";

    public static Boolean isSkippingIf() {
        return isSkippingIf;
    }

    public static void setIsSkippingIf(Boolean isSkippingIf) {
        IfTracker.isSkippingIf = isSkippingIf;
        IfTracker.ifReference = "";
    }

    public static String getIfReference() {
        return ifReference;
    }

    public static void setIfReference(String ifReference) {
        IfTracker.ifReference = ifReference;
    }
}
