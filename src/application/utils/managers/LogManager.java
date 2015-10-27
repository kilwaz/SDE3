package application.utils.managers;


import application.Main;
import application.error.Error;
import application.log.LogClass;
import application.log.LogMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class LogManager {
    private static Logger log = Logger.getLogger(LogManager.class);

    private ObservableList<LogClass> logClasses = FXCollections.observableArrayList();

    private static LogManager instance;
    private String logOutputFilePath = null;

    public LogManager() {
        instance = this;
    }

    public static LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    public ObservableList<LogClass> getLogClasses() {
        return logClasses;
    }

    public String getLogOutputFilePath() {
        if (logOutputFilePath == null) {
            DateTime dt = new DateTime();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("ddMMMyyyy HHmmss");
            String str = fmt.print(dt);

            String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            try {
                logOutputFilePath = URLDecoder.decode(path + "../../../logs/", "UTF-8") + "SDE3 - Started " + str + ".log";
            } catch (UnsupportedEncodingException ex) {
                Error.LOG_OUTPUT.record().create(ex);
            }
        }

        return logOutputFilePath;
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
