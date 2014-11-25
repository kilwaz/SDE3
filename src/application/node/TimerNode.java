package application.node;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.Program;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimerNode extends DrawableNode {
    private Color fillColour = Color.PLUM;
    private Integer milliSecsWait = 0;

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

        HBox hbox = new HBox(5);
        hbox.setLayoutY(55);
        hbox.setLayoutX(11);
        hbox.setAlignment(Pos.CENTER);

        Label timeToWaitFieldLabel = new Label();
        timeToWaitFieldLabel.setText("Time to wait: ");

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

        hbox.getChildren().add(timeToWaitFieldLabel);
        hbox.getChildren().add(timeToWaitField);
        hbox.getChildren().add(msLabel);

        anchorPane.getChildren().add(hbox);
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

    public void run(Boolean whileWaiting, HashMap<String, Object> map) {
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

    @Override
    public Color getFillColour() {
        return fillColour;
    }

    @Override
    public void setFillColour(Color fillColour) {
        this.fillColour = fillColour;
    }
}
