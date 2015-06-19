package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.Program;
import application.node.design.DrawableNode;
import application.utils.JobManager;
import application.utils.NodeRunParams;
import application.utils.TimerJob;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.quartz.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimerNode extends DrawableNode {
    private Integer milliSecsWait = 0;
    private String startOnDate = "";

    // This will make a copy of the node passed to it
    public TimerNode(TimerNode timerNode) {
        this.setId(-1);
        this.setX(timerNode.getX());
        this.setY(timerNode.getY());
        this.setWidth(timerNode.getWidth());
        this.setHeight(timerNode.getHeight());
        this.setColor(timerNode.getColor());
        this.setScale(timerNode.getScale());
        this.setContainedText(timerNode.getContainedText());
        this.setProgramId(timerNode.getProgramId());
        this.setNextNodeToRun(timerNode.getNextNodeToRun());

        this.setMilliSecsWait(timerNode.getMilliSecsWait());
    }

    public TimerNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public TimerNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
    }

    public TimerNode(Double x, Double y, Double width, Double height, Color color, String containedText, Integer programId, Integer id) {
        super(x, y, width, height, color, containedText, programId, id);
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        // The ordering here is Tab < ScrollPane < AnchorPane
        Tab tab = controller.createDefaultNodeTab(this);
        ScrollPane scrollPane = new ScrollPane();
        AnchorPane anchorPane = (AnchorPane) tab.getContent(); // We get the Anchor pane from the default Tab and change it to a ScrollPane

        scrollPane.setContent(anchorPane);

        VBox vBox = new VBox(5);
        vBox.setLayoutY(55);
        vBox.setLayoutX(11);
        vBox.setAlignment(Pos.CENTER);

        // DATE PICKER ROW
        HBox dateHBox = new HBox(5);
        dateHBox.setAlignment(Pos.CENTER);

        DatePicker datePicker = new DatePicker();

        Label jobStartLabel = new Label();
        jobStartLabel.setText("Start Date: ");

        dateHBox.getChildren().add(jobStartLabel);
        dateHBox.getChildren().add(datePicker);

        // REPEAT ROW



        // DELAY ROW
        HBox delayHBox = new HBox(5);
        delayHBox.setAlignment(Pos.CENTER);

        Label delayFieldLabel = new Label();
        delayFieldLabel.setText("Delay: ");

        TextField timeToWaitField = new TextField();
        timeToWaitField.setId("fieldTimeToWait-" + getId());
        timeToWaitField.setText(milliSecsWait.toString());
        timeToWaitField.setPrefWidth(70);

        timeToWaitField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                Program program = DataBank.currentlyEditProgram;
                TimerNode nodeToUpdate = (TimerNode) program.getFlowController().getNodeById(Integer.parseInt(textField.getId().replace("fieldTimeToWait-", "")));
                nodeToUpdate.setMilliSecsWait(Integer.parseInt(textField.getText()));

                DataBank.saveNode(nodeToUpdate);
            }
        });

        Label msLabel = new Label();
        msLabel.setText("ms");

        delayHBox.getChildren().add(delayFieldLabel);
        delayHBox.getChildren().add(timeToWaitField);
        delayHBox.getChildren().add(msLabel);

        vBox.getChildren().add(dateHBox);
        vBox.getChildren().add(new Separator());
        vBox.getChildren().add(delayHBox);

        anchorPane.getChildren().add(vBox);
        tab.setContent(scrollPane);

        // Go back to the beginning and run the code to show the tab, it should now exist
        return tab;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.add(new SavableAttribute("MilliSecsWait", milliSecsWait.getClass().getName(), milliSecsWait));
        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void createJob(DrawableNode jobNode) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("node", jobNode);

        JobDetail timerJob = JobBuilder.newJob(TimerJob.class)
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("dummyTriggerName", "group1")
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds(5).repeatForever())
                .build();

        JobManager.getInstance().scheduleJob(timerJob, trigger);
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
}
