package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.utils.NodeRunParams;
import application.utils.managers.JobManager;
import application.utils.timers.TimerNodeJob;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.joda.time.DateTime;
import org.quartz.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimerNode extends DrawableNode {
    private Integer milliSecsWait = 0;
    private Integer startDateYear = 0;
    private Integer startDateMonth = 0;
    private Integer startDateDay = 0;
    private String startTime = "";

    private Integer endDateYear = 0;
    private Integer endDateMonth = 0;
    private Integer endDateDay = 0;
    private String endTime = "";
    private String repetitionCount = "";

    private String frequency = "";
    private String frequencyDurationCount = "";
    private String frequencyDuration = "";
    private String repetition = "";
    private String startChoice = "";

    private Label repeatFieldLabel;
    private ChoiceBox frequencyChoice;
    private ChoiceBox startWhenChoice;

    private ChoiceBox frequencyDurationChoice;
    private ChoiceBox repetitionChoice;
    private TextField frequencyDurationCountField;

    private DatePicker dateUntilPicker;
    private TextField endTimeField;
    private Label endTimeHelpLabel;

    private TextField repetitionField;
    private Label timesHelpLabel;

    private Label jobStartLabel;
    private DatePicker datePicker;
    private TextField startTimeField;
    private Label startTimeHelpLabel;

    private HBox repeatHBox;
    private HBox startChoiceHBox;

    private TimerNode instance;

    // This will make a copy of the node passed to it
    public TimerNode(TimerNode timerNode) {
        this.setX(timerNode.getX());
        this.setY(timerNode.getY());
        this.setWidth(timerNode.getWidth());
        this.setHeight(timerNode.getHeight());
        this.setColor(timerNode.getColor());
        this.setScale(timerNode.getScale());
        this.setContainedText(timerNode.getContainedText());
//        this.setProgramUuid(timerNode.getProgramUuid());
        this.setNextNodeToRun(timerNode.getNextNodeToRun());

        this.setMilliSecsWait(timerNode.getMilliSecsWait());
        this.setStartDateYear(timerNode.getStartDateYear());
        this.setStartDateMonth(timerNode.getStartDateMonth());
        this.setStartDateDay(timerNode.getStartDateDay());
        this.setStartTime(timerNode.getStartTime());

        this.setEndDateYear(timerNode.getEndDateYear());
        this.setEndDateMonth(timerNode.getEndDateMonth());
        this.setEndDateDay(timerNode.getEndDateDay());
        this.setEndTime(timerNode.getEndTime());
        this.setRepetitionCount(timerNode.getRepetitionCount());

        this.setFrequency(timerNode.getFrequency());
        this.setFrequencyDurationCount(timerNode.getFrequencyDurationCount());
        this.setFrequencyDuration(timerNode.getFrequencyDuration());
        this.setRepetition(timerNode.getRepetition());
        this.setStartChoice(timerNode.getStartChoice());
    }

    public TimerNode() {
        super();
    }

    public Tab createInterface() {
        instance = this;

        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        VBox vBox = new VBox(5);
        vBox.setLayoutY(55);
        vBox.setLayoutX(11);
        vBox.setAlignment(Pos.CENTER_LEFT);

        // DATE PICKER ROW
        startChoiceHBox = new HBox(5);
        startChoiceHBox.setAlignment(Pos.CENTER_LEFT);

        jobStartLabel = new Label();
        jobStartLabel.setText("Start: ");
        jobStartLabel.setMinWidth(100);

        startWhenChoice = new ChoiceBox();

        List<String> startChoiceList = new ArrayList<>();
        startChoiceList.add("At");
        startChoiceList.add("Instantly");

        startWhenChoice.setItems(FXCollections.observableList(startChoiceList));
        startWhenChoice.setValue(startChoice);
        startWhenChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                startChoice = newValue;
                setVisibility(); // Needs to be called after new value is set

                instance.save();
            }
        });

        datePicker = new DatePicker();
        if (!(startDateYear == 0 && startDateMonth == 0 && startDateDay == 0)) {
            datePicker.setValue(LocalDate.of(startDateYear, startDateMonth, startDateDay));
        }
        datePicker.setOnAction(event -> {
            LocalDate date = datePicker.getValue();

            startDateYear = date.getYear();
            startDateMonth = date.getMonthValue();
            startDateDay = date.getDayOfMonth();

            instance.save();
        });

        startTimeField = new TextField();
        startTimeField.setText(startTime);
        startTimeField.setPrefWidth(65);

        startTimeField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();

            startTime = textField.getText();
            save();
        });

        startTimeHelpLabel = new Label();
        startTimeHelpLabel.setText("(hh:mm:ss as 24h clock)");
        startTimeHelpLabel.setMinWidth(100);

        // REPEAT ROW
        repeatHBox = new HBox(5);
        repeatHBox.setAlignment(Pos.CENTER_LEFT);

        repeatFieldLabel = new Label();
        repeatFieldLabel.setText("Repeat: ");
        repeatFieldLabel.setMinWidth(100);

        frequencyChoice = new ChoiceBox();

        List<String> frequencyList = new ArrayList<>();
        frequencyList.add("Every");
        frequencyList.add("Once");

        frequencyChoice.setItems(FXCollections.observableList(frequencyList));
        frequencyChoice.setValue(frequency);
        frequencyChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                frequency = newValue;
                setVisibility(); // Needs to be called after new value is set

                instance.save();
            }
        });

        frequencyDurationCountField = new TextField();
        frequencyDurationCountField.setText(frequencyDurationCount);
        frequencyDurationCountField.setPrefWidth(55);

        frequencyDurationCountField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();

            frequencyDurationCount = textField.getText();
            save();
        });

        frequencyDurationChoice = new ChoiceBox();

        List<String> frequencyDurationList = new ArrayList<>();
        frequencyDurationList.add("Days");
        frequencyDurationList.add("Hours");
        frequencyDurationList.add("Minutes");
        frequencyDurationList.add("Seconds");
        frequencyDurationList.add("Milliseconds");

        frequencyDurationChoice.setItems(FXCollections.observableList(frequencyDurationList));
        frequencyDurationChoice.setValue(frequencyDuration);
        frequencyDurationChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                frequencyDuration = newValue;
                instance.save();
            }
        });

        repetitionChoice = new ChoiceBox();

        List<String> repetitionList = new ArrayList<>();
        repetitionList.add("Until Date/Time");
        repetitionList.add("Until Repeated");
        repetitionList.add("Forever");

        repetitionChoice.setItems(FXCollections.observableList(repetitionList));
        repetitionChoice.setValue(repetition);
        repetitionChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                repetition = newValue;

                setVisibility(); // Needs to be called after new value is set
                instance.save();
            }
        });

        dateUntilPicker = new DatePicker();
        if (!(endDateYear == 0 && endDateMonth == 0 && endDateDay == 0)) {
            dateUntilPicker.setValue(LocalDate.of(endDateYear, endDateMonth, endDateDay));
        }
        dateUntilPicker.setOnAction(event -> {
            LocalDate date = dateUntilPicker.getValue();

            endDateYear = date.getYear();
            endDateMonth = date.getMonthValue();
            endDateDay = date.getDayOfMonth();

            instance.save();
        });

        endTimeField = new TextField();
        endTimeField.setText(endTime);
        endTimeField.setPrefWidth(65);

        endTimeField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();

            endTime = textField.getText();
            instance.save();
        });

        endTimeHelpLabel = new Label();
        endTimeHelpLabel.setText("(hh:mm:ss as 24h clock)");
        endTimeHelpLabel.setMinWidth(100);

        repetitionField = new TextField();
        repetitionField.setText(repetitionCount);
        repetitionField.setPrefWidth(65);

        repetitionField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();

            repetitionCount = textField.getText();
            instance.save();
        });

        timesHelpLabel = new Label();
        timesHelpLabel.setText("times");
        timesHelpLabel.setMinWidth(100);

        // DELAY ROW
        HBox delayHBox = new HBox(5);
        delayHBox.setAlignment(Pos.CENTER_LEFT);

        Label delayFieldLabel = new Label();
        delayFieldLabel.setText("Delay: ");
        delayFieldLabel.setMinWidth(100);

        TextField timeToWaitField = new TextField();
        timeToWaitField.setId("fieldTimeToWait-" + getUuidStringWithoutHyphen());
        timeToWaitField.setText(milliSecsWait.toString());
        timeToWaitField.setPrefWidth(70);

        timeToWaitField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();

            milliSecsWait = Integer.parseInt(textField.getText());
            instance.save();
        });

        Label msLabel = new Label();
        msLabel.setText("ms");

        delayHBox.getChildren().add(delayFieldLabel);
        delayHBox.getChildren().add(timeToWaitField);
        delayHBox.getChildren().add(msLabel);

        // Corrects the display as it has loaded to show/hide the correct interface
        setVisibility();

        vBox.getChildren().add(startChoiceHBox);
        vBox.getChildren().add(repeatHBox);
        vBox.getChildren().add(new Separator());
        vBox.getChildren().add(delayHBox);

        anchorPane.getChildren().add(vBox);

        // Go back to the beginning and run the code to show the tab, it should now exist
        return tab;
    }

    private void setVisibility() {
        // Rebuilds the display as needed
        repeatHBox.getChildren().clear();
        startChoiceHBox.getChildren().clear();

        repeatHBox.getChildren().add(repeatFieldLabel);
        repeatHBox.getChildren().add(frequencyChoice);

        startChoiceHBox.getChildren().add(jobStartLabel);
        startChoiceHBox.getChildren().add(startWhenChoice);

        if ("At".equals(startChoice)) {
            datePicker.setVisible(true);
            startTimeField.setVisible(true);
            startTimeHelpLabel.setVisible(true);

            startChoiceHBox.getChildren().add(datePicker);
            startChoiceHBox.getChildren().add(startTimeField);
            startChoiceHBox.getChildren().add(startTimeHelpLabel);
        } else if ("Instantly".equals(startChoice)) {
            datePicker.setVisible(false);
            startTimeField.setVisible(false);
            startTimeHelpLabel.setVisible(false);
        }

        if ("Once".equals(frequency)) {
            frequencyDurationChoice.setVisible(false);
            repetitionChoice.setVisible(false);
            frequencyDurationCountField.setVisible(false);
            dateUntilPicker.setVisible(false);
            endTimeField.setVisible(false);
            endTimeHelpLabel.setVisible(false);
            repetitionField.setVisible(false);
            timesHelpLabel.setVisible(false);
        } else if ("Every".equals(frequency)) {
            frequencyDurationChoice.setVisible(true);
            repetitionChoice.setVisible(true);
            frequencyDurationCountField.setVisible(true);

            repeatHBox.getChildren().add(frequencyDurationCountField);
            repeatHBox.getChildren().add(frequencyDurationChoice);
            repeatHBox.getChildren().add(repetitionChoice);

            if ("Until Date/Time".equals(repetition)) {
                dateUntilPicker.setVisible(true);
                endTimeField.setVisible(true);
                endTimeHelpLabel.setVisible(true);

                repetitionField.setVisible(false);
                timesHelpLabel.setVisible(false);

                repeatHBox.getChildren().add(dateUntilPicker);
                repeatHBox.getChildren().add(endTimeField);
                repeatHBox.getChildren().add(endTimeHelpLabel);
            } else if ("Until Repeated".equals(repetition)) {
                dateUntilPicker.setVisible(false);
                endTimeField.setVisible(false);
                endTimeHelpLabel.setVisible(false);

                repetitionField.setVisible(true);
                timesHelpLabel.setVisible(true);

                repeatHBox.getChildren().add(repetitionField);
                repeatHBox.getChildren().add(timesHelpLabel);
            } else if ("Forever".equals(repetition)) {
                dateUntilPicker.setVisible(false);
                endTimeField.setVisible(false);
                endTimeHelpLabel.setVisible(false);

                repetitionField.setVisible(false);
                timesHelpLabel.setVisible(false);
            }
        }
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        // MilliSecsWait
        SavableAttribute milliSecsWaitAttribute = SavableAttribute.create(SavableAttribute.class);
        milliSecsWaitAttribute.init("MilliSecsWait", milliSecsWait.getClass().getName(), milliSecsWait, this);
        savableAttributes.add(milliSecsWaitAttribute);

        // FrequencyDurationCount
        SavableAttribute frequencyDurationCountAttribute = SavableAttribute.create(SavableAttribute.class);
        frequencyDurationCountAttribute.init("FrequencyDurationCount", frequencyDurationCount.getClass().getName(), frequencyDurationCount, this);
        savableAttributes.add(frequencyDurationCountAttribute);

        // FrequencyDuration
        SavableAttribute frequencyDurationAttribute = SavableAttribute.create(SavableAttribute.class);
        frequencyDurationAttribute.init("FrequencyDuration", frequencyDuration.getClass().getName(), frequencyDuration, this);
        savableAttributes.add(frequencyDurationAttribute);

        // Frequency
        SavableAttribute frequencyAttribute = SavableAttribute.create(SavableAttribute.class);
        frequencyAttribute.init("Frequency", frequency.getClass().getName(), frequency, this);
        savableAttributes.add(frequencyAttribute);

        // Repetition
        SavableAttribute repetitionAttribute = SavableAttribute.create(SavableAttribute.class);
        repetitionAttribute.init("Repetition", repetition.getClass().getName(), repetition, this);
        savableAttributes.add(repetitionAttribute);

        // StartDateYear
        SavableAttribute StartDateYearAttribute = SavableAttribute.create(SavableAttribute.class);
        StartDateYearAttribute.init("StartDateYear", startDateYear.getClass().getName(), startDateYear, this);
        savableAttributes.add(StartDateYearAttribute);
        // StartDateMonth
        SavableAttribute startDateMonthAttribute = SavableAttribute.create(SavableAttribute.class);
        startDateMonthAttribute.init("StartDateMonth", startDateMonth.getClass().getName(), startDateMonth, this);
        savableAttributes.add(startDateMonthAttribute);
        // StartDateDay
        SavableAttribute startDateDayAttribute = SavableAttribute.create(SavableAttribute.class);
        startDateDayAttribute.init("StartDateDay", startDateDay.getClass().getName(), startDateDay, this);
        savableAttributes.add(startDateDayAttribute);
        // StartTime
        SavableAttribute startTimeAttribute = SavableAttribute.create(SavableAttribute.class);
        startTimeAttribute.init("StartTime", startTime.getClass().getName(), startTime, this);
        savableAttributes.add(startTimeAttribute);

        // EndDateYear
        SavableAttribute endDateYearAttribute = SavableAttribute.create(SavableAttribute.class);
        endDateYearAttribute.init("EndDateYear", endDateYear.getClass().getName(), endDateYear, this);
        savableAttributes.add(endDateYearAttribute);
        // EndDateMonth
        SavableAttribute endDateMonthAttribute = SavableAttribute.create(SavableAttribute.class);
        endDateMonthAttribute.init("EndDateMonth", endDateMonth.getClass().getName(), endDateMonth, this);
        savableAttributes.add(endDateMonthAttribute);
        // EndDateDay
        SavableAttribute endDateDayAttribute = SavableAttribute.create(SavableAttribute.class);
        endDateDayAttribute.init("EndDateDay", endDateDay.getClass().getName(), endDateDay, this);
        savableAttributes.add(endDateDayAttribute);
        // EndTime
        SavableAttribute endTimeAttribute = SavableAttribute.create(SavableAttribute.class);
        endTimeAttribute.init("EndTime", endTime.getClass().getName(), endTime, this);
        savableAttributes.add(endTimeAttribute);

        // RepetitionCount
        SavableAttribute repetitionCountAttribute = SavableAttribute.create(SavableAttribute.class);
        repetitionCountAttribute.init("RepetitionCount", repetitionCount.getClass().getName(), repetitionCount, this);
        savableAttributes.add(repetitionCountAttribute);

        // StartChoice
        SavableAttribute startChoiceAttribute = SavableAttribute.create(SavableAttribute.class);
        startChoiceAttribute.init("StartChoice", startChoice.getClass().getName(), startChoice, this);
        savableAttributes.add(startChoiceAttribute);

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void createJob(DrawableNode jobNode) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("node", jobNode);

        JobDetail timerJob = JobBuilder.newJob(TimerNodeJob.class)
                .usingJobData(jobDataMap)
                .build();

        TriggerBuilder triggerBuilder = TriggerBuilder
                .newTrigger();

        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();

        if ("Milliseconds".equals(frequencyDuration)) {
            simpleScheduleBuilder.withIntervalInMilliseconds(Integer.parseInt(frequencyDurationCount));
        } else if ("Seconds".equals(frequencyDuration)) {
            simpleScheduleBuilder.withIntervalInSeconds(Integer.parseInt(frequencyDurationCount));
        } else if ("Minutes".equals(frequencyDuration)) {
            simpleScheduleBuilder.withIntervalInMinutes(Integer.parseInt(frequencyDurationCount));
        } else if ("Hours".equals(frequencyDuration)) {
            simpleScheduleBuilder.withIntervalInHours(Integer.parseInt(frequencyDurationCount));
        } else if ("Days".equals(frequencyDuration)) {
            simpleScheduleBuilder.withIntervalInHours(Integer.parseInt(frequencyDurationCount) * 24);
        }

        if ("Until Repeated".equals(repetition)) {
            simpleScheduleBuilder.withRepeatCount(Integer.parseInt(repetitionCount));
        } else if ("Until Date/Time".equals(repetition)) {
            String[] endTimeSplit = endTime.split(":");

            DateTime endDate = new DateTime(
                    endDateYear,
                    endDateMonth,
                    endDateDay, Integer.parseInt(endTimeSplit[0]),   // Hours
                    Integer.parseInt(endTimeSplit[1]),               // Minutes
                    Integer.parseInt(endTimeSplit[2]));              // Seconds

            simpleScheduleBuilder.repeatForever();
            triggerBuilder.endAt(endDate.toDate());
        }

        if ("At".equals(startChoice)) {
            String[] startTimeSplit = startTime.split(":");

            DateTime startDate = new DateTime(
                    startDateYear,
                    startDateMonth,
                    startDateDay, Integer.parseInt(startTimeSplit[0]),   // Hours
                    Integer.parseInt(startTimeSplit[1]),                 // Minutes
                    Integer.parseInt(startTimeSplit[2]));                // Seconds

            triggerBuilder.startAt(startDate.toDate());
        } else if ("Instantly".equals(startChoice)) {
            triggerBuilder.startNow();
        }

        JobManager.getInstance().scheduleJob(timerJob, triggerBuilder.withSchedule(simpleScheduleBuilder).build());
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliSecsWait);
        } catch (InterruptedException e) {
            // Time to resume..
        }
    }

    public Integer getMilliSecsWait() {
        return milliSecsWait;
    }

    public void setMilliSecsWait(Integer milliSecsWait) {
        this.milliSecsWait = milliSecsWait;
    }

    public String getFrequencyDurationCount() {
        return frequencyDurationCount;
    }

    public void setFrequencyDurationCount(String frequencyDurationCount) {
        this.frequencyDurationCount = frequencyDurationCount;
    }

    public String getFrequencyDuration() {
        return frequencyDuration;
    }

    public void setFrequencyDuration(String frequencyDuration) {
        this.frequencyDuration = frequencyDuration;
    }

    public String getRepetition() {
        return repetition;
    }

    public void setRepetition(String repetition) {
        this.repetition = repetition;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getStartDateYear() {
        return startDateYear;
    }

    public void setStartDateYear(Integer startDateYear) {
        this.startDateYear = startDateYear;
    }

    public Integer getStartDateMonth() {
        return startDateMonth;
    }

    public void setStartDateMonth(Integer startDateMonth) {
        this.startDateMonth = startDateMonth;
    }

    public Integer getStartDateDay() {
        return startDateDay;
    }

    public void setStartDateDay(Integer startDateDay) {
        this.startDateDay = startDateDay;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Integer getEndDateYear() {
        return endDateYear;
    }

    public void setEndDateYear(Integer endDateYear) {
        this.endDateYear = endDateYear;
    }

    public Integer getEndDateMonth() {
        return endDateMonth;
    }

    public void setEndDateMonth(Integer endDateMonth) {
        this.endDateMonth = endDateMonth;
    }

    public Integer getEndDateDay() {
        return endDateDay;
    }

    public void setEndDateDay(Integer endDateDay) {
        this.endDateDay = endDateDay;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRepetitionCount() {
        return repetitionCount;
    }

    public void setRepetitionCount(String repetitionCount) {
        this.repetitionCount = repetitionCount;
    }

    public String getStartChoice() {
        return startChoice;
    }

    public void setStartChoice(String startChoice) {
        this.startChoice = startChoice;
    }
}
