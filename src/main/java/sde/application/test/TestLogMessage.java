package sde.application.test;

import sde.application.data.model.DatabaseObject;
import sde.application.test.core.TestCase;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TestLogMessage extends DatabaseObject {
    private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("kk:mm:ss.SSS dd MMM yyyy");
    private String message;
    private DateTime dateTime;
    private TestCase parent;

    public TestLogMessage() {
        super();
        dateTime = new DateTime();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getFormattedDateTime() {
        return dateFormatter.print(dateTime);
    }

    public TestCase getParent() {
        return parent;
    }

    public void setParentTestCase(TestCase parent) {
        this.parent = parent;
    }

    public String getParentUuid() {
        if (parent != null) {
            return parent.getUuidString();
        }
        return null;
    }
}
