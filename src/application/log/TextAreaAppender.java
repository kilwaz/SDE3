package application.log;

import application.gui.window.LogWindow;
import javafx.scene.control.TextArea;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

public class TextAreaAppender extends WriterAppender {
    private static volatile TextArea textArea = null;

    @Override
    public void append(final LoggingEvent loggingEvent) {
        final String message = this.layout.format(loggingEvent);

        LogMessage logMessage = new LogMessage(message, loggingEvent.getLogger().getName(), loggingEvent.getTimeStamp());
        LogManager.getInstance().addLogMessage(logMessage);

        // Update all of the log windows which are open watching this
        LogWindow.getLogWindows().forEach(application.gui.window.LogWindow::forceRefresh);
    }
}