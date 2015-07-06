package application.log;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LogManager {
    private ObservableList<LogClass> logClasses = FXCollections.observableArrayList();

    private static LogManager instance;

    public LogManager() {
        instance = this;
    }

    public static LogManager getInstance() {
        return instance;
    }

    public ObservableList<LogClass> getLogClasses() {
        return logClasses;
    }

    public void addLogMessage(LogMessage logMessage) {
        // Because we are changing the log classes object we need to make sure this is done in a safe thread
        Platform.runLater(() -> {
            Boolean found = false;
            // Find a more efficient way to do this?
            for (LogClass logClass : logClasses) {
                if (logClass.getClassName().equals(logMessage.getLoggerName())) {
                    logClass.addLogMessage(logMessage);
                    found = true;
                }
            }

            if (!found) {
                LogClass newClass = new LogClass(logMessage.getLoggerName());
                newClass.addLogMessage(logMessage);
                logClasses.add(newClass);
            }
        });
    }
}
