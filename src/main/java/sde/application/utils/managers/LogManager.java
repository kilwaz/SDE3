package sde.application.utils.managers;


import sde.application.Main;
import sde.application.error.Error;
import sde.application.log.LogClass;
import sde.application.log.LogMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import sde.application.utils.AppParams;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class LogManager {
    private static Logger log = Logger.getLogger(LogManager.class);
    private static LogManager instance;
    private ObservableList<LogClass> logClasses = FXCollections.observableArrayList();
    private String logOutputFilePath = null;

    public LogManager() {
        instance = this;

        File logDirectory = new File(getLogOutputDirectoryPath());
        if (!logDirectory.exists()) {
            logDirectory.mkdir();
        }
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

    public String getLogOutputDirectoryPath() {
        if ("".equals(AppParams.getLogDirectory())) {
            String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            try {
                path = URLDecoder.decode(path + "../../../logs/", "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Error.LOG_OUTPUT.record().create(ex);
            }

            return path;
        } else {
            return AppParams.getLogDirectory();
        }
    }

    public String getLogOutputDirectoryCanonicalPath() {
        try {
            return new File(getLogOutputDirectoryPath()).getCanonicalPath();
        } catch (IOException ex) {
            Error.SDE_FILE_NOT_FOUND.record().additionalInformation("Unable to find log directory path").create(ex);
        }
        return "Unable to find log file directory";
    }

    public String getLogOutputCanonicalPath() {
        try {
            return new File(getLogOutputFilePath()).getCanonicalPath();
        } catch (IOException ex) {
            Error.SDE_FILE_NOT_FOUND.record().additionalInformation("Unable to find log file path").create(ex);
        }
        return "Unable to find log file path";
    }

    public String getLogOutputFilePath() {
        if (logOutputFilePath == null) {
            DateTime dt = new DateTime();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("ddMMMyyyy HHmmss");
            String str = fmt.print(dt);
            logOutputFilePath = getLogOutputDirectoryPath() + "SDE3 - Started " + str + ".log";
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
