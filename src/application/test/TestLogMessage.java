package application.test;

import application.data.model.DatabaseObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TestLogMessage extends DatabaseObject {
    private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("kk:mm:ss.SSS dd MMM yyyy");
    private String message;
    private DateTime dateTime;

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

    public String getFormattedDateTime() {
        return dateFormatter.print(dateTime);
    }
}
