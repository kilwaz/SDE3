package application.error;

import application.utils.managers.ErrorManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class RecordedError {
    private final int code;
    private final String description;
    private Exception exception;
    private DateTime occurredAt;
    private int lineNumber = -1;
    private String className = "Unknown";
    private List<String> additionalInformation = new ArrayList<>();
    private String name;
    private Boolean printStackTrace = true;

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

        if (exception != null && printStackTrace) {
            log.log(Error.class.getCanonicalName(), Level.ERROR, this.toString(), exception);
        } else {
            log.log(Error.class.getCanonicalName(), Level.ERROR, this.toString(), null);
        }

        return this;
    }

    public RecordedError additionalInformation(String additionalInformation) {
        this.additionalInformation.add(additionalInformation);
        return this;
    }

    public RecordedError hideStackInLog() {
        this.printStackTrace = false;
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

    public List<String> getAdditionalInformation() {
        return additionalInformation;
    }

    @Override
    public String toString() {
        StringBuilder additionalInfoBuilder = new StringBuilder();
        for (String s : additionalInformation) {
            additionalInfoBuilder.append(s).append("\n\t\t");
        }
        return "#" + code + ":" + name + ": " + description + (additionalInformation == null ? "" : "\r\n\tAdditional Information:\r\n\t\t" + additionalInfoBuilder.toString());
    }
}
