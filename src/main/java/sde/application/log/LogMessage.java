package sde.application.log;

public class LogMessage {
    private String logMessage = "";
    private String loggerName = "";
    private Long timeStamp = 0l;

    public LogMessage(String logMessage, String loggerName, long timeStamp) {
        this.logMessage = logMessage;
        this.loggerName = loggerName;
        this.timeStamp = timeStamp;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }
}
