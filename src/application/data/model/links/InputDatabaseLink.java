package application.data.model.links;

import application.data.model.DatabaseLink;
import application.node.objects.Input;

public class InputDatabaseLink extends DatabaseLink {
    public InputDatabaseLink() {
        super("input", Input.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuidFromString", String.class)); // 1
        link("node_id", method("getParentUuid"), null); // 2
        link("variable_name", method("getVariableName"), method("setVariableName", String.class)); // 3
        link("variable_value", method("getVariableValueLimited"), method("setVariableValue", String.class)); // 4
    }
}
