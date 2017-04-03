package sde.application.log;


import java.util.ArrayList;
import java.util.List;

public class LogClass {
    private String className = "";
    private String simpleName = "";
    private List<LogMessage> logMessages = new ArrayList<>();

    public LogClass(String className) {
        this.className = className;
        if (className.contains(".")) {
            this.simpleName = className.substring(className.lastIndexOf(".") + 1);
        } else {
            this.simpleName = className;
        }
    }

    public String getClassName() {
        return className;
    }

    public void addLogMessage(LogMessage logMessage) {
        logMessages.add(logMessage);
    }

    public List<LogMessage> getLogMessages() {
        return logMessages;
    }

    public String toString() {
        return "" + this.simpleName + " - (" + logMessages.size() + ")";
    }
}
