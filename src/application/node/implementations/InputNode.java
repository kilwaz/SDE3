package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.Program;
import application.gui.SDETextField;
import application.node.design.DrawableNode;
import application.node.objects.Input;
import application.utils.SDEUtils;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;
import org.controlsfx.control.textfield.TextFields;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class InputNode extends DrawableNode {
    private List<Input> inputs = new ArrayList<>();

    private VBox inputRows;
    private InputNode instance;

    private static Logger log = Logger.getLogger(InputNode.class);

    // This will make a copy of the node passed to it
    public InputNode(InputNode inputNode) {
        this.setX(inputNode.getX());
        this.setY(inputNode.getY());
        this.setWidth(inputNode.getWidth());
        this.setHeight(inputNode.getHeight());
        this.setColor(inputNode.getColor());
        this.setScale(inputNode.getScale());
        this.setContainedText(inputNode.getContainedText());
//        this.setProgramUuid(inputNode.getProgramUuid());
        this.setNextNodeToRun(inputNode.getNextNodeToRun());

        // This copies all of the inputs and creates new object for each one using the copy constructor
        inputs.addAll(inputNode.getInputs().stream().map(loopInput -> new Input(loopInput, this)).collect(Collectors.toList()));
    }

    public InputNode(){
        super();
    }

    public void updateInputVariableName(Integer inputId, String variableName) {
        for (Input input : inputs) {
            if (input.getUuidString().equals(inputId)) {
                input.setVariableName(variableName);
                input.save();
                break;
            }
        }
    }

    public void updateInputVariableValue(Integer inputId, String variableValue) {
        for (Input input : inputs) {
            if (input.getUuidString().equals(inputId)) {
                input.setVariableValue(variableValue);
                input.save();
                break;
            }
        }
    }

    public Tab createInterface() {
        this.instance = this;

        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        inputRows = new VBox(5);
        inputRows.setLayoutY(55);
        inputRows.setLayoutX(11);

        if (inputs.size() < 1) {
            // Automatically assigned to this triggerNode via 'this' reference
            DataBank.createNewInput("", "", this);
            save();
        }

        for (Input input : inputs) {
            inputRows.getChildren().add(createInputNodeRow(input));
        }

        inputRows.getChildren().add(createAddInputNodeRow());

        anchorPane.getChildren().add(inputRows);

        return tab;
    }

    public HBox createInputNodeRow(Input input) {
        HBox inputRow = new HBox(5);
        inputRow.setId("inputRow-" + input.getUuidString() + "-" + getUuidString());
        inputRow.setAlignment(Pos.CENTER);

        // Remove input button
        Button deleteInput = AwesomeDude.createIconButton(AwesomeIcon.MINUS);
        deleteInput.setPrefWidth(35);
        deleteInput.setTooltip(new Tooltip("Delete this input"));
        deleteInput.setId("deleteInputButton-" + input.getUuidString() + "-" + getUuidString());
        deleteInput.setOnAction(event -> {
            Button deleteButton = (Button) event.getSource();
            Program program = DataBank.currentlyEditProgram;
            String[] fieldId = deleteButton.getId().split("-");
            InputNode inputNode = (InputNode) program.getFlowController().getNodeById(fieldId[2]);

            // Remove the input
            inputNode.removeInput(inputNode.getInputById(fieldId[1]));
        });
        inputRow.getChildren().add(deleteInput);

        Label inputNameLabel = new Label();
        TextField inputNameField = TextFields.createClearableTextField();

        inputNameLabel.setPrefWidth(50);
        inputNameLabel.setText("Name");
        inputNameLabel.setId("inputNameLabel-" + input.getUuidString() + "-" + getUuidString());

        inputNameField.setText(input.getVariableName());
        inputNameField.setId("inputNameField-" + input.getUuidString() + "-" + getUuidString());
        inputNameField.setPrefWidth(200);
        inputNameField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();

            if (!textField.getText().isEmpty()) {
                String[] fieldId = textField.getId().split("-");

                updateInputVariableName(Integer.parseInt(fieldId[1]), textField.getText());

                this.save();
                Controller.getInstance().updateCanvasControllerNow();
                SDETextField.setToSaved(textField);
            }
        });

        Label inputValueLabel = new Label();
        TextField inputValueField = TextFields.createClearableTextField();

        inputValueLabel.setPrefWidth(30);
        inputValueLabel.setText("value");
        inputValueLabel.setId("inputValueLabel-" + input.getUuidString() + "-" + getUuidString());

        inputValueField.setText(input.getVariableValue());
        inputValueField.setPrefWidth(200);
        inputValueField.setId("inputValueField-" + input.getUuidString() + "-" + getUuidString());
        inputValueField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                String[] fieldId = textField.getId().split("-");

                updateInputVariableValue(Integer.parseInt(fieldId[1]), textField.getText());

                this.save();
                Controller.getInstance().updateCanvasControllerNow();
            }
        });

        inputRow.getChildren().add(inputNameLabel);
        inputRow.getChildren().add(inputNameField);
        inputRow.getChildren().add(inputValueLabel);
        inputRow.getChildren().add(inputValueField);

        return inputRow;
    }

    public void removeInput(Input input) {
        inputs.remove(input);
        input.delete();

        // Removes the row off of the UI
        Node rowToRemove = null;
        for (Node node : inputRows.getChildren()) {
            if (node.getId().equals("inputRow-" + input.getUuidString() + "-" + getUuidString())) {
                rowToRemove = node;
            }
        }

        if (rowToRemove != null) {
            inputRows.getChildren().remove(rowToRemove);
        }
    }

    public HBox createAddInputNodeRow() {
        HBox addInputRow = new HBox(5);
        addInputRow.setId("addInputRow-" + getUuidString());

        Button addButton = AwesomeDude.createIconButton(AwesomeIcon.PLUS);
        addButton.setPrefWidth(35);
        addButton.setTooltip(new Tooltip("Add new input"));

        addButton.setOnAction(event -> {
            Input newInput = DataBank.createNewInput("", "", instance);

            // Add the new switch just above the plus button
            inputRows.getChildren().add(inputRows.getChildren().size() - 1, createInputNodeRow(newInput));
        });

        addInputRow.getChildren().add(addButton);

        return addInputRow;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public Element getXMLRepresentation(Document document) {
        Element nodeElement = super.getXMLRepresentation(document);

        // Create a new element to save all inputs inside
        Element inputsElement = document.createElement("Inputs");

        for (Input input : inputs) {
            Element inputElement = document.createElement("Input");

            Element variableNameElement = document.createElement("VariableName");
            variableNameElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(input.getVariableName())));

            Element variableValueElement = document.createElement("VariableValue");
            variableValueElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(input.getVariableValue())));

            inputElement.appendChild(variableNameElement);
            inputElement.appendChild(variableValueElement);
            inputsElement.appendChild(inputElement);
        }

        nodeElement.appendChild(inputsElement);

        return nodeElement;
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

    // Sets all inputs with a specific name to a single new value
    public void setInputsByName(String name, String newValue) {
        inputs.stream().filter(input -> input.getVariableName().equals(name)).forEach(input -> input.setVariableValue(newValue));
    }

    public Input getInputByName(String name) {
        for (Input input : inputs) {
            if (input.getVariableName().equals(name)) {
                return input;
            }
        }

        return null;
    }

    public Input getInputById(String uuid) {
        for (Input input : inputs) {
            if (input.getUuidString().equals(uuid)) {
                return input;
            }
        }

        return null;
    }
}
