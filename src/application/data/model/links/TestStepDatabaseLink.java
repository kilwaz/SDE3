package application.data.model.links;

import application.data.model.DatabaseLink;
import application.test.TestStep;

public class TestStepDatabaseLink extends DatabaseLink {
    public TestStepDatabaseLink() {
        super("test_step", TestStep.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuidFromString", String.class)); // 1
        link("test_string", method("getTestString"), method("setTestString", String.class)); // 2
        link("expected_equal", method("getExpectedEqual"), method("setExpectedEqual", String.class)); // 3
        link("observed_equal", method("getObservedEqual"), method("setObservedEqual", String.class)); // 4
        linkBlob("screenshot", method("getScreenshotInputStream"), null); // 5
        link("successful", method("getSuccessful"), method("setSuccessful", Boolean.class)); // 6
        link("test_result", method(""), null); // 7
        link("test_type", method("getTestType"), method("setTestType", Integer.class)); // 8
    }
}
