package application.data.model.links;

import application.data.model.DatabaseLink;
import application.test.TestStep;

import java.util.UUID;

public class TestResultDatabaseLink extends DatabaseLink {
    public TestResultDatabaseLink() {
        super("test_result", TestStep.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
    }
}
