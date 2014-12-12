package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.node.objects.Input;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.textfield.TextFields;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InputNode extends DrawableNode {
    private List<Input> inputs = new ArrayList<>();

    // This will make a copy of the node passed to it
    public InputNode(InputNode inputNode) {
        this.setId(-1);
        this.setX(inputNode.getX());
        this.setY(inputNode.getY());
        this.setWidth(inputNode.getWidth());
        this.setHeight(inputNode.getHeight());
        this.setColor(inputNode.getColor());
        this.setScale(inputNode.getScale());
        this.setContainedText(inputNode.getContainedText());
        this.setProgramId(inputNode.getProgramId());
        this.setNextNodeToRun(inputNode.getNextNodeToRun());

        // This copies all of the inputs and creates new object for each one using the copy constructor
        inputs.addAll(inputNode.getInputs().stream().map(loopInput -> new Input(loopInput, this)).collect(Collectors.toList()));
    }

    public InputNode(Integer id, Integer programId) {
        super(id, programId);
        DataBank.loadInputs(this);
    }

    public InputNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        DataBank.loadInputs(this);
    }

    public InputNode(Double x, Double y, Double width, Double height, Color color, String containedText, Integer programId, Integer id) {
        super(x, y, width, height, color, containedText, programId, id);
        DataBank.loadInputs(this);
    }

    public void updateInputVariableName(Integer inputId, String variableName) {
        for (Input input : inputs) {
            if (input.getId().equals(inputId)) {
                input.setVariableName(variableName);
                DataBank.saveInput(input);
                break;
            }
        }
    }

    public void updateInputVariableValue(Integer inputId, String variableValue) {
        for (Input input : inputs) {
            if (input.getId().equals(inputId)) {
                input.setVariableValue(variableValue);
                DataBank.saveInput(input);
                break;
            }
        }
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        if (inputs.size() < 1) {
            // Automatically assigned to this triggerNode via 'this' reference
            DataBank.createNewInput("", "", this);
            DataBank.saveNode(this);
        }

        for (Input input : inputs) {
            rows.getChildren().add(createInputNodeRow(input));
        }
        anchorPane.getChildren().add(rows);

        return tab;
    }

    public HBox createInputNodeRow(Input input) {
        HBox inputRow = new HBox(5);
        inputRow.setAlignment(Pos.CENTER);
        Label inputNameLabel = new Label();
        TextField inputNameField = TextFields.createClearableTextField();

        inputNameLabel.setPrefWidth(50);
        inputNameLabel.setText("Name");
        inputNameLabel.setId("inputNameLabel-" + input.getId() + "-" + getId());

        inputNameField.setText(input.getVariableName());
        inputNameField.setId("inputNameField-" + input.getId() + "-" + getId());
        inputNameField.setPrefWidth(80);
        inputNameField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();

            if (!textField.getText().isEmpty()) {
                String[] fieldId = textField.getId().split("-");

                updateInputVariableName(Integer.parseInt(fieldId[1]), textField.getText());

                DataBank.saveNode(this);
                Controller.getInstance().updateCanvasControllerNow();
            }
        });

        Label inputValueLabel = new Label();
        TextField inputValueField = TextFields.createClearableTextField();

        inputValueLabel.setPrefWidth(30);
        inputValueLabel.setText("value");
        inputValueLabel.setId("inputValueLabel-" + input.getId() + "-" + getId());

        inputValueField.setText(input.getVariableValue());
        inputValueField.setPrefWidth(80);
        inputValueField.setId("inputValueField-" + input.getId() + "-" + getId());
        inputValueField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                String[] fieldId = textField.getId().split("-");

                updateInputVariableValue(Integer.parseInt(fieldId[1]), textField.getText());

                DataBank.saveNode(this);
                Controller.getInstance().updateCanvasControllerNow();
            }
        });

        inputRow.getChildren().add(inputNameLabel);
        inputRow.getChildren().add(inputNameField);
        inputRow.getChildren().add(inputValueLabel);
        inputRow.getChildren().add(inputValueField);

        return inputRow;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<SavableAttribute>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void addInput(Input input) {
        inputs.add(input);
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }
}
