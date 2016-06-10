package application.data.model.links;

import application.data.model.DatabaseLink;
import application.node.objects.Test;
import application.test.TestCommand;

import java.util.UUID;

public class TestCommandDatabaseLink extends DatabaseLink {
    public TestCommandDatabaseLink() {
        super("test_command", TestCommand.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("test_id", method("getParentUuid"), method("setParentTest", Test.class)); // 2
        link("main_command", method("getMainCommand"), method("setMainCommand", String.class)); // 3
        link("raw_command", method("getRawCommand"), method("setRawCommand", String.class)); // 4
        link("command_position", method("getCommandPosition"), method("setCommandPosition", Integer.class)); // 5
        linkBlob("screenshot", method("getScreenshotInputStream"), null); // 6
    }
}
