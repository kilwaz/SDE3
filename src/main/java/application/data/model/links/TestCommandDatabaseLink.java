package application.data.model.links;

import application.data.model.DatabaseLink;
import application.node.objects.Test;
import application.test.TestCommand;
import application.test.core.TestCase;

import java.util.UUID;

public class TestCommandDatabaseLink extends DatabaseLink {
    public TestCommandDatabaseLink() {
        super("test_command", TestCommand.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("test_id", method("getParentUuid"), method("setParentTest", Test.class)); // 2
        link("main_command", method("getMainCommand"), method("setMainCommand", String.class)); // 3
        link("raw_command", method("getRawCommand"), method("setRawCommand", String.class)); // 4
        link("command_line_number", method("getCommandLineNumber"), method("setCommandLineNumber", Integer.class)); // 5
        link("command_order", method("getCommandOrder"), method("setCommandOrder", Integer.class)); // 6
        link("test_case_parent_id", method("getParentTestCaseUuid"), method("setParentTestCase", TestCase.class)); // 7
    }
}
