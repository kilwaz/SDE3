package application.utils;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apache.log4j.Logger;

import java.util.HashMap;

public class StatisticStore {
    private static Logger log = Logger.getLogger(StatisticStore.class);

    private SimpleIntegerProperty requests;
    private SimpleIntegerProperty applicationStarts;
    private SimpleIntegerProperty upTime;
    private SimpleStringProperty upTimeFormatted;

    private SimpleLongProperty requestSize;
    private SimpleStringProperty requestSizeFormatted;

    private SimpleLongProperty responseSize;
    private SimpleStringProperty responseSizeFormatted;

    private HashMap<Integer, SimpleIntegerProperty> responseCodes;

    private SimpleIntegerProperty commands;

    public StatisticStore() {
        resetStatistics();
    }

    public int getRequests() {
        return requests.get();
    }

    public void setRequests(int requests) {
        this.requests.set(requests);
    }

    public SimpleIntegerProperty requestsProperty() {
        return requests;
    }

    public int getApplicationStarts() {
        return applicationStarts.get();
    }

    public void setApplicationStarts(int applicationStarts) {
        this.applicationStarts.set(applicationStarts);
    }

    public SimpleIntegerProperty applicationStartsProperty() {
        return applicationStarts;
    }

    public int getUpTime() {
        return upTime.get();
    }

    public void setUpTime(int upTime) {
        this.upTime.set(upTime);
    }

    public SimpleIntegerProperty upTimeProperty() {
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

    public int getCommands() {
        return commands.get();
    }

    public void setCommands(int commands) {
        this.commands.set(commands);
    }

    public SimpleIntegerProperty commandsProperty() {
        return commands;
    }

    public void resetStatistics() {
        requests = new SimpleIntegerProperty();
        applicationStarts = new SimpleIntegerProperty();
        upTime = new SimpleIntegerProperty();
        upTimeFormatted = new SimpleStringProperty();

        requestSize = new SimpleLongProperty();
        requestSizeFormatted = new SimpleStringProperty();

        responseSize = new SimpleLongProperty();
        responseSizeFormatted = new SimpleStringProperty();

        responseCodes = new HashMap<>();

        commands = new SimpleIntegerProperty();

        addRequestSize(0);
        addResponseSize(0);
    }

    public HashMap<Integer, SimpleIntegerProperty> getResponseCodes() {
        return responseCodes;
    }

    public void incrementResponseCode(Integer code) {
        SimpleIntegerProperty codeProperty = responseCodes.get(code);
        if (codeProperty == null) {
            log.info("Creating new stat for code " + code);
            codeProperty = responseCodes.putIfAbsent(code, new SimpleIntegerProperty());
        }
        Platform.runLater(new StatisticStore.StatisticIntegerUpdate(codeProperty, 1));
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
        Platform.runLater(new StatisticIntegerUpdate(requestsProperty(), 1));
    }

    public void incrementCommands() {
        Platform.runLater(new StatisticIntegerUpdate(commandsProperty(), 1));
    }

    public void incrementApplicationStart() {
        Platform.runLater(new StatisticIntegerUpdate(applicationStartsProperty(), 1));
    }

    public void incrementUpTime() {
        Platform.runLater(new StatisticIntegerUpdate(upTimeProperty(), 1));
        int totalSecs = upTimeProperty().get();
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;
        Platform.runLater(new StatisticStringUpdate(upTimeFormattedProperty(), String.format("%02d:%02d:%02d", hours, minutes, seconds)));
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
        private SimpleIntegerProperty property;
        private Integer increaseAmount;

        private StatisticIntegerUpdate(SimpleIntegerProperty property, Integer increaseAmount) {
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

        public void run() {
            if (property != null) {
                property.set(property.get() + increaseAmount);
            }
        }
    }
}
