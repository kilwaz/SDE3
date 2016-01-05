package application.node.implementations;

import application.data.SavableAttribute;
import application.data.model.dao.TriggerDAO;
import application.gui.Controller;
import application.gui.FlowController;
import application.node.design.DrawableNode;
import application.node.objects.Trigger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import org.controlsfx.control.textfield.TextFields;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TriggerNode extends DrawableNode {
    private List<Trigger> triggers = null;
    private static Logger log = Logger.getLogger(TriggerNode.class);

    // This will make a copy of the node passed to it
    public TriggerNode(TriggerNode triggerNode) {
        this.setX(triggerNode.getX());
        this.setY(triggerNode.getY());
        this.setWidth(triggerNode.getWidth());
        this.setHeight(triggerNode.getHeight());
        this.setColor(triggerNode.getColor());
        this.setScale(triggerNode.getScale());
        this.setContainedText(triggerNode.getContainedText());
        //this.setProgramUuid(triggerNode.getProgramUuid());
        this.setNextNodeToRun(triggerNode.getNextNodeToRun());

        // This copies all of the switches and creates new object for each one using the copy constructor
        getTriggers().addAll(triggerNode.getTriggers().stream().map(loopTrigger -> new Trigger(loopTrigger, this)).collect(Collectors.toList()));
    }

    public TriggerNode() {
        super();
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        if (getTriggers().size() < 1) {
            // Automatically assigned to this triggerNode via 'this' reference
            Trigger trigger = Trigger.create(Trigger.class);
            trigger.setParent(this);
            trigger.setThen("");
            trigger.setWhen("");
            trigger.setWatch("");
            trigger.save();
            addTrigger(trigger);
            save();
        }

        for (Trigger trigger : getTriggers()) {
            rows.getChildren().add(createTriggerNodeRow(trigger));
        }

        anchorPane.getChildren().add(rows);

        return tab;
    }

    public HBox createTriggerNodeRow(Trigger trigger) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER);
        Label watchLabel = new Label();
        TextField watchField = TextFields.createClearableTextField();
        DrawableNode currentWatchedNode = FlowController.getNodeFromContainedText(trigger.getWatch());

        watchLabel.setPrefWidth(40);
        watchLabel.setText("Watch");
        watchLabel.setId("triggerField-watchLabel" + "-" + this.getUuidString());

        watchField.setText(trigger.getWatch());
        watchField.setPrefWidth(80);
        watchField.setId("triggerField-watchField" + "-" + this.getUuidString());
        watchField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                trigger.setWatch(textField.getText());

                Controller controller = Controller.getInstance();
                ChoiceBox whenChoice = (ChoiceBox) controller.getElementById("triggerField-whenChoice" + "-" + this.getUuidString());
                ChoiceBox thenChoice = (ChoiceBox) controller.getElementById("triggerField-thenChoice" + "-" + this.getUuidString());

                DrawableNode watchedNode = FlowController.getNodeFromContainedText(textField.getText());
                if (watchedNode != null) {
                    if (whenChoice != null)
                        whenChoice.setItems(FXCollections.observableList(watchedNode.getAvailableTriggers()));
                    if (thenChoice != null)
                        thenChoice.setItems(FXCollections.observableList(watchedNode.getAvailableTriggerActions()));
                } else {
                    if (whenChoice != null) whenChoice.setItems(FXCollections.observableList(new ArrayList<>()));
                    if (thenChoice != null) thenChoice.setItems(FXCollections.observableList(new ArrayList<>()));
                }

                trigger.save();
                getProgram().getFlowController().checkConnections();
                Controller.getInstance().updateCanvasControllerNow();
            }
        });

        Label whenLabel = new Label();
        ChoiceBox whenChoice = new ChoiceBox();

        whenLabel.setPrefWidth(35);
        whenLabel.setText("when");
        whenLabel.setId("triggerField-whenLabel" + "-" + this.getUuidString());

        whenChoice.setPrefWidth(120);
        whenChoice.setId("triggerField-whenChoice" + "-" + this.getUuidString());
        if (currentWatchedNode != null) {
            whenChoice.setItems(FXCollections.observableList(currentWatchedNode.getAvailableTriggers()));
            whenChoice.getSelectionModel().select(trigger.getWhen());
        }
        whenChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null && !newValue.isEmpty()) {
                    trigger.setWhen(newValue);
                    trigger.save();
                }

                log.info("Selected when item: " + newValue);
            }
        });

        Label thenLabel = new Label();
        ChoiceBox thenChoice = new ChoiceBox();

        thenLabel.setPrefWidth(30);
        thenLabel.setText("then");
        thenLabel.setId("triggerField-thenLabel" + "-" + this.getUuidString());

        thenChoice.setPrefWidth(120);
        thenChoice.setId("triggerField-thenChoice" + "-" + this.getUuidString());
        if (currentWatchedNode != null) {
            thenChoice.setItems(FXCollections.observableList(currentWatchedNode.getAvailableTriggerActions()));
            thenChoice.getSelectionModel().select(trigger.getThen());
        }
        thenChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null && !newValue.isEmpty()) {
                    trigger.setThen(newValue);
                    trigger.save();
                }

                log.info("Selected then item: " + newValue);
            }
        });

        row.getChildren().add(watchLabel);
        row.getChildren().add(watchField);
        row.getChildren().add(whenLabel);
        row.getChildren().add(whenChoice);
        row.getChildren().add(thenLabel);
        row.getChildren().add(thenChoice);

        return row;
    }

    public void addTrigger(Trigger trigger) {
        getTriggers().add(trigger);
    }

    public void setTriggers(List<Trigger> triggers) {
        this.triggers = triggers;
    }

    public List<Trigger> getTriggers() {
        if (triggers == null) {
            TriggerDAO triggerDAO = new TriggerDAO();
            triggers = triggerDAO.getTriggersByNode(this);
        }

        return triggers;
    }
}
