package application.data.model.links;

import application.data.model.DatabaseLink;
import application.node.implementations.TestNode;
import application.node.objects.Test;

import java.util.UUID;

public class TestDatabaseLink extends DatabaseLink {
    public TestDatabaseLink() {
        super("test", Test.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("node_id", method("getParentUuid"), method("setParentTestNode", TestNode.class)); // 2
        link("text", method("getText"), method("setText", String.class)); // 3
    }
}
