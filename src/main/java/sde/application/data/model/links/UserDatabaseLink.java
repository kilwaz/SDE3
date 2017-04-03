package sde.application.data.model.links;

import sde.application.data.User;
import sde.application.data.model.DatabaseLink;
import sde.application.gui.Program;

import java.util.UUID;

public class UserDatabaseLink extends DatabaseLink {
    public UserDatabaseLink() {
        super("user", User.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("username", method("getUsername"), method("setUsername", String.class)); // 2
        link("last_program", method("getCurrentProgramUuid"), method("setCurrentProgram", Program.class)); // 3
    }
}
