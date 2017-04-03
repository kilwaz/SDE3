package application.log;

import application.error.Error;
import application.gui.window.LogWindow;
import application.utils.AppParams;
import application.utils.managers.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import java.io.*;

public class TextAreaAppender extends WriterAppender {
    private static Logger log = Logger.getLogger(TextAreaAppender.class);

    @Override
    public void append(final LoggingEvent loggingEvent) {

        final String message = this.layout.format(loggingEvent);
        String stackTrace = "";

        if (loggingEvent.getThrowableInformation() != null) {
            stackTrace = getStackTrace(loggingEvent.getThrowableInformation().getThrowable());
        }

        if (AppParams.getInAppLogView()) { // Only user the in app logger if it is enabled - has a memory impact
            LogMessage logMessage = new LogMessage(message + stackTrace, loggingEvent.getLogger().getName(), loggingEvent.getTimeStamp());
            LogManager.getInstance().addLogMessage(logMessage);
        }

        // Update all of the log windows which are open watching this
        LogWindow.getLogWindows().forEach(application.gui.window.LogWindow::forceRefresh);

        // Write the log to a file
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(LogManager.getInstance().getLogOutputFilePath(), true)))) {
            out.print(message + stackTrace);
        } catch (IOException ex) {
            Error.LOG_APPENDER.record().create(ex);
        }
    }

    private String getStackTrace(Throwable t) {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(sWriter);
        t.printStackTrace(pWriter);
        return sWriter.toString();
    }
}