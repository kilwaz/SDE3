package application.data.model.links;

import application.data.User;
import application.data.model.DatabaseLink;
import application.error.Error;

public class UserDatabaseLink extends DatabaseLink {
    public UserDatabaseLink() {
        super("user");
        try {
            // Make sure the order is the same as column order in database
            link("username", User.class.getMethod("getUsername")); // 1
            link("last_program", User.class.getMethod("getCurrentProgramId")); // 2
        } catch (NoSuchMethodException ex) {
            Error.DATA_LINK_METHOD_NOT_FOUND.record().create(ex);
        }
    }
}
