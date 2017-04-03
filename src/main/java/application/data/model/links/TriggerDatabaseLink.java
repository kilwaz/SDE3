package application.data.model.links;

import application.data.model.DatabaseLink;
import application.node.implementations.TriggerNode;
import application.node.objects.Trigger;

public class TriggerDatabaseLink extends DatabaseLink {
    public TriggerDatabaseLink() {
        super("trigger_condition", Trigger.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuidFromString", String.class)); // 1
        link("node_id", method("getParentUuid"), method("setParent", TriggerNode.class)); // 2
        link("trigger_watch", method("getWatch"), method("setWatch", String.class)); // 3
        link("trigger_when", method("getWhen"), method("setWhen", String.class)); // 4
        link("trigger_then", method("getThen"), method("setThen", String.class)); // 5
    }
}
