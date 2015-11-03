package application.data.model.links;

import application.data.model.DatabaseLink;
import application.error.Error;
import application.node.objects.Trigger;

public class TriggerDatabaseLink extends DatabaseLink {
    public TriggerDatabaseLink() {
        super("trigger_condition");
        try {
            // Make sure the order is the same as column order in database
            link("node_id", Trigger.class.getMethod("getParentId")); // 1
            link("trigger_watch", Trigger.class.getMethod("getWatch")); // 2
            link("trigger_when", Trigger.class.getMethod("getWhen")); // 3
            link("trigger_then", Trigger.class.getMethod("getThen")); // 4
        } catch (NoSuchMethodException ex) {
            Error.DATA_LINK_METHOD_NOT_FOUND.record().create(ex);
        }
    }
}
