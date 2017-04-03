package sde.application.data.model.links;

import sde.application.data.SavableAttribute;
import sde.application.data.model.DatabaseLink;

import java.util.UUID;

public class SavableAttributeDatabaseLink extends DatabaseLink {
    public SavableAttributeDatabaseLink() {
        super("node_details", SavableAttribute.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("object_value", method("getVariable"), method("setVariable", Object.class)); // 2
        link("node_id", method("getParentUuid"), null); // 3
        link("object_name", method("getVariableName"), method("setVariableName", String.class)); // 4
        link("object_class", method("getClassName"), method("setClassName", String.class)); // 5
    }
}
