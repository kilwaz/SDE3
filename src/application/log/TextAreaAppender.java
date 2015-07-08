package application.log;

import application.gui.window.LogWindow;
import javafx.scene.control.TextArea;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TextAreaAppender extends WriterAppender {
    private static volatile TextArea textArea = null;

    @Override
    public void append(final LoggingEvent loggingEvent) {
        final String message = this.layout.format(loggingEvent);
        String stackTrace = "";

        if (loggingEvent.getThrowableInformation() != null) {
            stackTrace = getStackTrace(loggingEvent.getThrowableInformation().getThrowable());
        }

        LogMessage logMessage = new LogMessage(message + stackTrace, loggingEvent.getLogger().getName(), loggingEvent.getTimeStamp());
        LogManager.getInstance().addLogMessage(logMessage);

        // Update all of the log windows which are open watching this
        LogWindow.getLogWindows().forEach(application.gui.window.LogWindow::forceRefresh);
    }

    private String getStackTrace(Throwable t) {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(sWriter);
        t.printStackTrace(pWriter);
        return sWriter.toString();
    }
}