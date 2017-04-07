package sde.application.data.model.links;

import sde.application.data.model.DatabaseLink;
import sde.application.node.implementations.InputNode;
import sde.application.node.objects.Input;

import java.util.UUID;

public class InputDatabaseLink extends DatabaseLink {
    public InputDatabaseLink() {
        super("input", Input.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("node_id", method("getParentUuid"), method("setParent", InputNode.class)); // 2
        link("variable_name", method("getVariableName"), method("setVariableName", String.class)); // 3
        link("variable_value", method("getVariableValueLimited"), method("setVariableValue", String.class)); // 4
    }
}
