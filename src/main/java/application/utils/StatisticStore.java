package application.utils;

import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatisticStore {
    private static Logger log = Logger.getLogger(StatisticStore.class);

    private SimpleLongProperty requests;
    private SimpleLongProperty applicationStarts;
    private SimpleLongProperty programsStarted;
    private SimpleLongProperty upTime;
    private SimpleStringProperty upTimeFormatted;

    private SimpleLongProperty requestSize;
    private SimpleStringProperty requestSizeFormatted;

    private SimpleLongProperty responseSize;
    private SimpleStringProperty responseSizeFormatted;

    private HashMap<Integer, SimpleLongProperty> responseCodes;

    private SimpleLongProperty commands;

    public StatisticStore() {
        resetStatistics();
    }

    public long getRequests() {
        return requests.get();
    }

    public void setRequests(long requests) {
        this.requests.set(requests);
    }

    public SimpleLongProperty requestsProperty() {
        return requests;
    }

    public long getApplicationStarts() {
        return applicationStarts.get();
    }

    public void setApplicationStarts(long applicationStarts) {
        this.applicationStarts.set(applicationStarts);
    }

    public SimpleLongProperty applicationStartsProperty() {
        return applicationStarts;
    }

    public long getProgramsStarted() {
        return programsStarted.get();
    }

    public void setProgramsStarted(long programsStarted) {
        this.programsStarted.set(programsStarted);
    }

    public SimpleLongProperty programsStartedProperty() {
        return programsStarted;
    }

    public long getUpTime() {
        return upTime.get();
    }

    public void setUpTime(long upTime) {
        this.upTime.set(upTime);
    }

    public SimpleLongProperty upTimeProperty() {
        return upTime;
    }

    public String getUpTimeFormatted() {
        return upTimeFormatted.get();
    }

    public void setUpTimeFormatted(String upTimeFormatted) {
        this.upTimeFormatted.set(upTimeFormatted);
    }

    public SimpleStringProperty upTimeFormattedProperty() {
        return upTimeFormatted;
    }

    public long getRequestSize() {
        return requestSize.get();
    }

    public void setRequestSize(long requestSize) {
        this.requestSize.set(requestSize);
    }

    public SimpleLongProperty requestSizeProperty() {
        return requestSize;
    }

    public String getRequestSizeFormatted() {
        return requestSizeFormatted.get();
    }

    public void setRequestSizeFormatted(String requestSizeFormatted) {
        this.requestSizeFormatted.set(requestSizeFormatted);
    }

    public SimpleStringProperty requestSizeFormattedProperty() {
        return requestSizeFormatted;
    }

    public long getResponseSize() {
        return responseSize.get();
    }

    public void setResponseSize(long responseSize) {
        this.responseSize.set(responseSize);
    }

    public SimpleLongProperty responseSizeProperty() {
        return responseSize;
    }

    public String getResponseSizeFormatted() {
        return responseSizeFormatted.get();
    }

    public void setResponseSizeFormatted(String responseSizeFormatted) {
        this.responseSizeFormatted.set(responseSizeFormatted);
    }

    public SimpleStringProperty responseSizeFormattedProperty() {
        return responseSizeFormatted;
    }

    public long getCommands() {
        return commands.get();
    }

    public void setCommands(long commands) {
        this.commands.set(commands);
    }

    public SimpleLongProperty commandsProperty() {
        return commands;
    }

    public void resetStatistics() {
        requests = new SimpleLongProperty();
        applicationStarts = new SimpleLongProperty();
        programsStarted = new SimpleLongProperty();
        upTime = new SimpleLongProperty();
        upTimeFormatted = new SimpleStringProperty();

        requestSize = new SimpleLongProperty();
        requestSizeFormatted = new SimpleStringProperty();

        responseSize = new SimpleLongProperty();
        responseSizeFormatted = new SimpleStringProperty();

        responseCodes = new HashMap<>();

        commands = new SimpleLongProperty();

        addRequestSize(0);
        addResponseSize(0);
    }

    public HashMap<Integer, SimpleLongProperty> getResponseCodes() {
        return responseCodes;
    }

    public void incrementResponseCode(Integer code) {
        SimpleLongProperty codeProperty = responseCodes.get(code);
        if (codeProperty == null) {
            //log.info("Creating new stat for code " + code);
            codeProperty = responseCodes.putIfAbsent(code, new SimpleLongProperty());
        }
        Platform.runLater(new StatisticStore.StatisticLongUpdate(codeProperty, 1));
    }

    public void addResponseSize(long responseSize) {
        Platform.runLater(new StatisticStore.StatisticLongUpdate(responseSizeProperty(), responseSize));
        Platform.runLater(new StatisticStore.StatisticStringUpdate(responseSizeFormattedProperty(), humanReadableByteCount(responseSizeProperty().get(), false)));
    }

    public void addRequestSize(long requestSize) {
        Platform.runLater(new StatisticStore.StatisticLongUpdate(requestSizeProperty(), requestSize));
        Platform.runLater(new StatisticStore.StatisticStringUpdate(requestSizeFormattedProperty(), humanReadableByteCount(requestSizeProperty().get(), false)));
    }

    public void incrementRequests() {
        Platform.runLater(new StatisticLongUpdate(requestsProperty(), 1));
    }

    public void incrementCommands() {
        Platform.runLater(new StatisticLongUpdate(commandsProperty(), 1));
    }

    public void incrementApplicationStart() {
        Platform.runLater(new StatisticLongUpdate(applicationStartsProperty(), 1));
    }

    public void incrementProgramStart() {
        Platform.runLater(new StatisticLongUpdate(programsStartedProperty(), 1));
    }

    public void incrementUpTime() {
        Platform.runLater(new StatisticIntegerUpdate(upTimeProperty(), 1));
        Long totalSecs = upTimeProperty().get();
        Long days = totalSecs / 86400;
        Long hours = (totalSecs % 86400) / 3600;
        Long minutes = (totalSecs % 3600) / 60;
        Long seconds = totalSecs % 60;
        String formatString = "";
        List<Long> timeValues = new ArrayList<>();
        if (days > 0) { // Include days if time has been that long
            timeValues.add(days);
            if (days > 1) {
                formatString += "%s days";
            } else {
                formatString += "%s day";
            }
        }
        if (hours > 0) { // Include hours if time has been that long
            timeValues.add(hours);
            formatString += " %sh";
        }
        if (minutes > 0) { // Include minutes if time has been that long
            timeValues.add(minutes);
            formatString += " %sm";
        }
        if (seconds > 0) { // Include seconds if time has been that long
            timeValues.add(seconds);
            formatString += " %ss";
        }

        Platform.runLater(new StatisticStringUpdate(upTimeFormattedProperty(), String.format(formatString, timeValues.toArray())));
    }

    private String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private class StatisticStringUpdate implements Runnable {
        private SimpleStringProperty property;
        private String newString;

        private StatisticStringUpdate(SimpleStringProperty property, String newString) {
            this.property = property;
            this.newString = newString;
        }

        public void run() {
            if (property != null) {
                property.set(newString);
            }
        }
    }

    private class StatisticIntegerUpdate implements Runnable {
        private SimpleLongProperty property;
        private Integer increaseAmount;

        private StatisticIntegerUpdate(SimpleLongProperty property, Integer increaseAmount) {
            this.property = property;
            this.increaseAmount = increaseAmount;
        }

        public void run() {
            if (property != null) {
                property.set(property.get() + increaseAmount);
            }
        }
    }

    private class StatisticLongUpdate implements Runnable {
        private SimpleLongProperty property;
        private Long increaseAmount;

        private StatisticLongUpdate(SimpleLongProperty property, Long increaseAmount) {
            this.property = property;
            this.increaseAmount = increaseAmount;
        }

        private StatisticLongUpdate(SimpleLongProperty property, Integer increaseAmount) {
            this.property = property;
            this.increaseAmount = increaseAmount.longValue();
        }

        public void run() {
            if (property != null) {
                property.set(property.get() + increaseAmount);
            }
        }
    }
}
