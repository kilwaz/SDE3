package application.data.model.links;

import application.data.model.DatabaseLink;
import application.test.core.TestSet;
import application.test.core.TestSetBatch;

import java.util.UUID;

public class TestSetDatabaseLink extends DatabaseLink {
    public TestSetDatabaseLink() {
        super("test_set", TestSet.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("test_id", method("getTestID"), method("setTestID", String.class)); // 2
        link("test_description", method("getTestDescription"), method("setTestDescription", String.class)); // 3
        link("test_set_batch_parent_id", method("getParentTestSetBatchUuid"), method("testSetBatch", TestSetBatch.class)); // 4
    }
}
