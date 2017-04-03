package application.data.model.links;

import application.data.model.DatabaseLink;
import application.node.implementations.SwitchNode;
import application.node.objects.Switch;

import java.util.UUID;

public class SwitchDatabaseLink extends DatabaseLink {
    public SwitchDatabaseLink() {
        super("switch", Switch.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("node_id", method("getParentUuid"), method("setParent", SwitchNode.class)); // 2
        link("target", method("getTarget"), method("setTarget", String.class)); // 3
        link("enabled", method("isEnabled"), method("setEnabled", Boolean.class)); // 4
    }
}
