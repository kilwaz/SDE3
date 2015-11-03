package application.data.model.links;

import application.data.model.DatabaseLink;
import application.error.Error;
import application.node.objects.Input;

public class InputDatabaseLink extends DatabaseLink {
    public InputDatabaseLink() {
        super("input");
        try {
            // Make sure the order is the same as column order in database
            link("node_id", Input.class.getMethod("getParentId")); // 1
            link("variable_name", Input.class.getMethod("getVariableName")); // 2
            link("variable_value", Input.class.getMethod("getVariableValueLimited")); // 3
        } catch (NoSuchMethodException ex) {
            Error.DATA_LINK_METHOD_NOT_FOUND.record().create(ex);
        }
    }
}
