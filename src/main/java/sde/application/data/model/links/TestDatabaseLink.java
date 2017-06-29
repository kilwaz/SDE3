package sde.application.data.model.links;

import sde.application.data.model.DatabaseLink;
import sde.application.node.implementations.TestNode;
import sde.application.node.objects.Test;

import java.util.UUID;

public class TestDatabaseLink extends DatabaseLink {
    public TestDatabaseLink() {
        super("test", Test.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("node_id", method("getParentUuid"), method("setParentTestNode", TestNode.class)); // 2
        link("text", method("getText"), method("setText", String.class)); // 3
        link("web_driver_id", method("getWebDriverId"), method("setWebDriverId", String.class)); // 4
        linkBlob("recording_file", method("getRecordingFileInputStream"), method("setRecordingFile", Object.class)); // 5

        child(TestCommandDatabaseLink.class, "test_id");
    }
}
