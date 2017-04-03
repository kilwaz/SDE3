package application.data.model.links;

import application.data.model.DatabaseLink;
import application.node.implementations.TestManagerNode;
import application.test.core.TestSetBatch;
import org.joda.time.DateTime;

import java.util.UUID;

public class TestSetBatchDatabaseLink extends DatabaseLink {
    public TestSetBatchDatabaseLink() {
        super("test_set_batch", TestSetBatch.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("node_id", method("getParentUuid"), method("setParent", TestManagerNode.class)); // 2
        link("created_time", method("getCreatedTime"), method("setCreatedTime", DateTime.class)); // 3
    }
}
