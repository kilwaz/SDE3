package application.data.model.links;

import application.data.User;
import application.data.model.DatabaseLink;
import application.gui.Program;

import java.util.UUID;

public class ProgramDatabaseLink extends DatabaseLink {
    public ProgramDatabaseLink() {
        super("program", Program.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("name", method("getName"), method("setName", String.class)); // 2
        link("start_node", method("getStartNodeUuid"), null); // 3
        link("view_offset_width", method("getViewOffsetWidth"), method("setViewOffsetWidth", Double.class)); // 4
        link("view_offset_height", method("getViewOffsetHeight"), method("setViewOffsetHeight", Double.class)); // 5
        link("user_id", method("getParentUserUuid"), method("setParentUser", User.class)); // 6
    }
}
