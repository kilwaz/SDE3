package application.data.model.links;

import application.data.model.DatabaseLink;
import application.utils.CustomObject;

public class CustomObjectDatabaseLink extends DatabaseLink {
    public CustomObjectDatabaseLink() {
        super("serialized", CustomObject.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuidFromString", String.class)); // 1
        link("node_id", method("getParentUuid"), null); // 2
        linkBlob("serial_object", method("getPayLoadInputStream"), method("setPayload", Object.class)); // 3
        link("serial_reference", method("getPayLoadReference"), method("setPayLoadReference", String.class)); // 4
    }
}
