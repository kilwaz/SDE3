package application.node.implementations;

import application.data.SavableAttribute;
import application.data.model.dao.InputDAO;
import application.gui.Controller;
import application.gui.SDETextField;
import application.node.design.DrawableNode;
import application.node.objects.Input;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import org.controlsfx.control.textfield.TextFields;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InputNode extends DrawableNode {
    private static Logger log = Logger.getLogger(InputNode.class);
    private List<Input> inputs = null;
    private VBox inputRows;
    private InputNode instance;

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
        getInputs().addAll(inputNode.getInputs().stream().map(loopInput -> new Input(loopInput, this)).collect(Collectors.toList()));
    }

    public InputNode() {
        super();
    }

    public void updateInputVariableName(String uuidString, String variableName) {
        for (Input input : getInputs()) {
            if (input.getUuidStringWithoutHyphen().equals(uuidString)) {
                input.setVariableName(variableName);
                input.save();
                break;
            }
        }
    }

    public void updateInputVariableValue(String uuidString, String variableValue) {
        for (Input input : getInputs()) {
            if (input.getUuidStringWithoutHyphen().equals(uuidString)) {
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

        if (getInputs().size() < 1) {
            // Automatically assigned to this triggerNode via 'this' reference
            Input newInput = Input.create(Input.class);
            newInput.setParent(this);
            newInput.save();
            addInput(newInput);
            save();
        }

        for (Input input : getInputs()) {
            inputRows.getChildren().add(createInputNodeRow(input));
        }

        inputRows.getChildren().add(createAddInputNodeRow());

        anchorPane.getChildren().add(inputRows);

        return tab;
    }

    public HBox createInputNodeRow(Input input) {
        HBox inputRow = new HBox(5);
        inputRow.setId("inputRow-" + input.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen());
        inputRow.setAlignment(Pos.CENTER);

        // Remove input button
        Button deleteInput = new Button();
        deleteInput.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.MINUS).build());
        deleteInput.setPrefWidth(35);
        deleteInput.setTooltip(new Tooltip("Delete this input"));
        deleteInput.setId("deleteInputButton-" + input.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen());
        deleteInput.setOnAction(event -> {
            Button deleteButton = (Button) event.getSource();
            String[] fieldId = deleteButton.getId().split("-");
            InputNode inputNode = (InputNode) getProgram().getFlowController().getNodeByUuidWithoutHyphen(fieldId[2]);

            // Remove the input
            inputNode.removeInput(inputNode.getInputById(fieldId[1]));
        });
        inputRow.getChildren().add(deleteInput);

        Label inputNameLabel = new Label();
        TextField inputNameField = TextFields.createClearableTextField();

        inputNameLabel.setPrefWidth(50);
        inputNameLabel.setText("Name");
        inputNameLabel.setId("inputNameLabel-" + input.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen());

        inputNameField.setText(input.getVariableName());
        inputNameField.setId("inputNameField-" + input.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen());
        inputNameField.setPrefWidth(200);
        inputNameField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();

            if (!textField.getText().isEmpty()) {
                String[] fieldId = textField.getId().split("-");

                updateInputVariableName(fieldId[1], textField.getText());

                this.save();
                Controller.getInstance().updateCanvasControllerNow();
                SDETextField.setToSaved(textField);
            }
        });

        Label inputValueLabel = new Label();
        TextField inputValueField = TextFields.createClearableTextField();

        inputValueLabel.setPrefWidth(30);
        inputValueLabel.setText("value");
        inputValueLabel.setId("inputValueLabel-" + input.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen());

        inputValueField.setText(input.getVariableValue());
        inputValueField.setPrefWidth(200);
        inputValueField.setId("inputValueField-" + input.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen());
        inputValueField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                String[] fieldId = textField.getId().split("-");

                updateInputVariableValue(fieldId[1], textField.getText());

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
        getInputs().remove(input);
        input.delete();

        // Removes the row off of the UI
        Node rowToRemove = null;
        for (Node node : inputRows.getChildren()) {
            if (node.getId().equals("inputRow-" + input.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen())) {
                rowToRemove = node;
            }
        }

        if (rowToRemove != null) {
            inputRows.getChildren().remove(rowToRemove);
        }
    }

    public HBox createAddInputNodeRow() {
        HBox addInputRow = new HBox(5);
        addInputRow.setId("addInputRow-" + getUuidStringWithoutHyphen());

        Button addButton = new Button();
        addButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.PLUS).build());
        addButton.setPrefWidth(35);
        addButton.setTooltip(new Tooltip("Add new input"));

        addButton.setOnAction(event -> {
            Input newInput = Input.create(Input.class);
            newInput.setParent(this);
            newInput.save();

            addInput(newInput);

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

    public void addInput(Input input) {
        getInputs().add(input);
    }

    public List<Input> getInputs() {
        if (inputs == null) {
            InputDAO inputDAO = new InputDAO();
            inputs = inputDAO.getInputsByNode(this);
        }

        return inputs;
    }

    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }

    // Sets all inputs with a specific name to a single new value
    public void setInputsByName(String name, String newValue) {
        getInputs().stream().filter(input -> input.getVariableName().equals(name)).forEach(input -> {
            input.setVariableValue(newValue);
            input.save();
        });
    }

    public Input getInputByName(String name) {
        for (Input input : getInputs()) {
            if (input.getVariableName().equals(name)) {
                return input;
            }
        }

        return null;
    }

    public Input getInputById(String uuid) {
        for (Input input : getInputs()) {
            if (input.getUuidStringWithoutHyphen().equals(uuid)) {
                return input;
            }
        }

        return null;
    }
}
