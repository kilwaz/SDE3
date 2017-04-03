package application.data.model.links;

import application.data.model.DatabaseLink;
import application.test.TestCommand;
import application.test.TestCommandScreenshot;

import java.util.UUID;

public class TestCommandScreenshotDatabaseLink extends DatabaseLink {
    public TestCommandScreenshotDatabaseLink() {
        super("test_command_screenshot", TestCommandScreenshot.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        linkBlob("screenshot", method("getScreenshotInputStream"), null); // 2
        link("test_command_parent_id", method("getParentTestCommandUuid"), method("setParentTestCommand", TestCommand.class)); // 3
    }
}