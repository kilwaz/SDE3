package sde.application.data.model.links;

import sde.application.data.model.DatabaseLink;
import sde.application.test.TestLogMessage;
import sde.application.test.core.TestCase;
import org.joda.time.DateTime;

import java.util.UUID;

public class TestLogMessageDatabaseLink extends DatabaseLink {
    public TestLogMessageDatabaseLink() {
        super("test_log_messages", TestLogMessage.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("test_case_parent_id", method("getParentUuid"), method("setParentTestCase", TestCase.class)); // 2
        link("log_message", method("getMessage"), method("setMessage", String.class)); // 3
        link("log_time", method("getDateTime"), method("setDateTime", DateTime.class)); // 4
    }
}
