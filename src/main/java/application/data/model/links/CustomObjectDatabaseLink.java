package application.data.model.links;

import application.data.model.DatabaseLink;
import application.node.implementations.CustomObjectNode;
import application.utils.CustomObject;

import java.util.UUID;

public class CustomObjectDatabaseLink extends DatabaseLink {
    public CustomObjectDatabaseLink() {
        super("serialized", CustomObject.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("node_id", method("getParentUuid"), method("setParent", CustomObjectNode.class)); // 2
        linkBlob("serial_object", method("getPayLoadInputStream"), method("setPayload", Object.class)); // 3
        link("serial_reference", method("getPayLoadReference"), method("setPayLoadReference", String.class)); // 4
    }
}
