package sde.application.data.model.links;

import sde.application.data.model.DatabaseLink;
import sde.application.gui.Program;
import sde.application.node.design.DrawableNode;

import java.util.UUID;

public class DrawableNodeDatabaseLink extends DatabaseLink {
    public DrawableNodeDatabaseLink() {
        super("node", DrawableNode.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("program_id", method("getProgramUuid"), method("setProgram", Program.class)); // 2
        link("node_type", method("getNodeType"), null); // 3
    }
}
