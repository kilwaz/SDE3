package application.error;

import application.utils.managers.ErrorManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by alex on 26/10/2015.
 */
public class RecordedError {

    private final int code;
    private final String description;
    private Exception exception;
    private DateTime occurredAt;
    private int lineNumber = -1;
    private String className = "Unknown";
    private String additionalInformation;
    private String name;

    protected RecordedError(int code, String description, String name) {
        this.code = code;
        this.description = description;
        this.name = name;
    }

    public RecordedError create() {
        return createError(null);
    }

    public RecordedError create(Exception exception) {
        return createError(exception);
    }

    private RecordedError createError(Exception exception) {
        this.exception = exception;
        occurredAt = new DateTime();
        ErrorManager.getInstance().addError(this);

        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTraces[3];

        Logger log = null;

        if (stackTraceElement != null) {
            lineNumber = stackTraceElement.getLineNumber();
            className = stackTraceElement.getClassName();

            log = Logger.getLogger(stackTraceElement.getClassName());
        }

        if (log == null) {
            log = Logger.getLogger(Error.class);
        }

        if (exception != null) {
            log.log(Error.class.getCanonicalName(), Level.ERROR, this.toString(), exception);
        } else {
            log.log(Error.class.getCanonicalName(), Level.ERROR, this.toString(), null);
        }

        return this;
    }

    public RecordedError additionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    public Exception getException() {
        return exception;
    }

    public String getOccurredAt() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss dd-MMM-yyyy");
        return fmt.print(occurredAt);
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getClassName() {
        return className;
    }

    public String getReference() {
        return name;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    @Override
    public String toString() {
        return "#" + code + ":" + name+ ": " + description + (additionalInformation == null ? "" : "\r\n\tAdditional Information:\r\n\t\t" + additionalInformation);
    }
}
