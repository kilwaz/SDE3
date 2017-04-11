package sde.application.node.objects;

import org.joda.time.DateTime;
import org.openqa.selenium.logging.LogEntry;

public class BrowserLog {

    private String logLevel;
    private String logMessage;
    private Long logDate;

    public BrowserLog(LogEntry logEntry) {
        logLevel = logEntry.getLevel().getName().intern();
        logMessage = logEntry.getMessage();
        logDate = logEntry.getTimestamp();
    }

    public String getLogLevel() {
        return logLevel;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public DateTime getLogDateTime() {
        return new DateTime(logDate);
    }
}
