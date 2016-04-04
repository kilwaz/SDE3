package application.node.implementations;

import application.data.SavableAttribute;
import application.data.model.dao.SwitchDAO;
import application.gui.Controller;
import application.gui.Program;
import application.node.design.DrawableNode;
import application.node.objects.Switch;
import application.utils.NodeRunParams;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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

public class SwitchNode extends DrawableNode {
    private static Logger log = Logger.getLogger(SwitchNode.class);
    private List<Switch> aSwitches = null;
    private VBox switchRows;

    // This will make a copy of the node passed to it
    public SwitchNode(SwitchNode switchNode) {
        this.setX(switchNode.getX());
        this.setY(switchNode.getY());
        this.setWidth(switchNode.getWidth());
        this.setHeight(switchNode.getHeight());
        this.setColor(switchNode.getColor());
        this.setScale(switchNode.getScale());
        this.setContainedText(switchNode.getContainedText());
//        this.setProgramUuid(switchNode.getProgramUuid());
        this.setNextNodeToRun(switchNode.getNextNodeToRun());

        // This copies all of the switches and creates new object for each one using the copy constructor
        getSwitches().addAll(switchNode.getSwitches().stream().map(loopSwitch -> new Switch(loopSwitch, this)).collect(Collectors.toList()));
    }

    public SwitchNode() {
        super();
    }

    public void updateSwitchTarget(String switchId, String target) {
        for (Switch aSwitch : getSwitches()) {
            if (aSwitch.getUuidStringWithoutHyphen().equals(switchId)) {
                aSwitch.setTarget(target);
                aSwitch.save();
                break;
            }
        }
    }

    public void updateSwitchEnabled(String switchId, Boolean enabled) {
        for (Switch aSwitch : getSwitches()) {
            if (aSwitch.getUuidStringWithoutHyphen().equals(switchId)) {
                aSwitch.setEnabled(enabled);
                aSwitch.save();
                break;
            }
        }
    }

    public List<Switch> getSwitches() {
        if (aSwitches == null) {
            SwitchDAO switchDAO = new SwitchDAO();
            aSwitches = switchDAO.getSwitchesByNode(this);
        }

        return aSwitches;
    }

    public void setSwitches(List<Switch> aSwitches) {
        this.aSwitches = aSwitches;
    }

    public Switch getSwitch(String id) {
        for (Switch aSwitch : getSwitches()) {
            if (aSwitch.getUuidStringWithoutHyphen().equals(id)) {
                return aSwitch;
            }
        }

        return null;
    }

    public void removeSwitch(Switch switchObj) {
        Switch switchToRemove = null;
        for (Switch aSwitch : getSwitches()) {
            if (aSwitch.getUuidString().equals(switchObj.getUuidString())) {
                switchToRemove = aSwitch;
            }
        }

        if (switchToRemove != null) {
            getSwitches().remove(switchToRemove);
        } else {
            log.info("Cannot find switch to remove with id " + switchObj.getUuidString());
        }
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void addSwitch(Switch aSwitch) {
        getSwitches().add(aSwitch);
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        for (Switch aSwitch : getSwitches()) {
            if (aSwitch.isEnabled()) {
                Program.runHelper(aSwitch.getTarget(), getProgram().getFlowController().getReferenceID(), this, whileWaiting, false, nodeRunParams);
            }
        }
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        switchRows = new VBox(5);
        switchRows.setLayoutY(55);
        switchRows.setLayoutX(11);

        for (Switch aSwitch : getSwitches()) {
            switchRows.getChildren().add(createSwitchNodeRow(aSwitch));
        }

        switchRows.getChildren().add(createAddSwitchNodeRow());
        anchorPane.getChildren().add(switchRows);

        // Go back to the beginning and run the code to show the tab, it should now exist
        return tab;
    }

    public HBox createSwitchNodeRow(Switch aSwitch) {
        HBox switchRow = new HBox(5);
        switchRow.setId("switchRow-" + aSwitch.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen());

        // Remove input button
        Button deleteSwitch = new Button();
        deleteSwitch.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.MINUS).build());
        deleteSwitch.setPrefWidth(35);
        deleteSwitch.setTooltip(new Tooltip("Delete this switch"));
        deleteSwitch.setId("deleteSwitchButton-" + aSwitch.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen());
        deleteSwitch.setOnAction(event -> {
            Button deleteButton = (Button) event.getSource();
            String[] fieldId = deleteButton.getId().split("-");
            SwitchNode switchNode = (SwitchNode) getProgram().getFlowController().getNodeByUuidWithoutHyphen(fieldId[2]);

            // Remove the switch
            deleteSwitch(switchNode, fieldId[1]);
        });
        switchRow.getChildren().add(deleteSwitch);

        // Tick / Cross button
        ToggleButton firstSwitchButton = new ToggleButton();

        firstSwitchButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.CLOSE).build());

        if (aSwitch.isEnabled()) {
            firstSwitchButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.CHECK).build());
            firstSwitchButton.setSelected(true);
        } else {
            firstSwitchButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.CLOSE).build());
            firstSwitchButton.setSelected(false);
        }
        firstSwitchButton.setPrefWidth(35);
        firstSwitchButton.setId("switchButton-" + aSwitch.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen());
        firstSwitchButton.setOnAction(event -> {
            ToggleButton toggleButton = (ToggleButton) event.getSource();
            String[] fieldId = toggleButton.getId().split("-");
            SwitchNode switchNode = (SwitchNode) getProgram().getFlowController().getNodeByUuidWithoutHyphen(fieldId[2]);

            if (toggleButton.isSelected()) {
                toggleButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.CHECK).build());
                toggleButton.setSelected(true);
                switchNode.updateSwitchEnabled(fieldId[1], true);
            } else {
                toggleButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.CLOSE).build());
                toggleButton.setSelected(false);
                switchNode.updateSwitchEnabled(fieldId[1], false);
            }

            getProgram().getFlowController().checkConnections(); // Toggling a switch will make or break connections
        });
        switchRow.getChildren().add(firstSwitchButton);

        // Text field
        TextField switchField = TextFields.createClearableTextField();
        //ValidationSupport validationSupport = new ValidationSupport();
        //validationSupport.registerValidator(switchField, Validator.createEqualsValidator("Node name does not exist", "test");
        //validationSupport.registerValidator(switchField, Validator.createEmptyValidator("SHOULD NOT BE EMPTY"));

        switchField.setText(aSwitch.getTarget()); // The text of the field should be set before linking it to auto complete to avoid jittery UI
        TextFields.bindAutoCompletion(switchField, getProgram().getFlowController().getNodes());

        switchField.setId("switchField-" + aSwitch.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen());
        switchField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            String[] fieldId = textField.getId().split("-");
            SwitchNode switchNode = (SwitchNode) getProgram().getFlowController().getNodeByUuidWithoutHyphen(fieldId[2]);
            if (!textField.getText().isEmpty()) {
                switchNode.updateSwitchTarget(fieldId[1], textField.getText());

                getProgram().getFlowController().checkConnections(); // Renaming a node might make or break connections

                switchNode.save();
            } else {
                deleteSwitch(switchNode, fieldId[1]);
            }

            Controller.getInstance().updateCanvasControllerNow();
        });

        switchRow.getChildren().add(switchField);

        return switchRow;
    }

    private void deleteSwitch(SwitchNode switchNode, String switchId) {
        Switch switchToRemove = switchNode.getSwitch(switchId);

        switchToRemove.delete();
        removeSwitch(switchToRemove);

        // Removes the row off of the UI
        Node rowToRemove = null;
        for (Node node : switchRows.getChildren()) {
            if (node.getId().equals("switchRow-" + switchToRemove.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen())) {
                rowToRemove = node;
            }
        }

        if (rowToRemove != null) {
            switchRows.getChildren().remove(rowToRemove);
        }

        getProgram().getFlowController().checkConnections();
    }

    public HBox createAddSwitchNodeRow() {
        HBox addSwitchRow = new HBox(5);
        addSwitchRow.setId("addSwitchRow-" + getUuidStringWithoutHyphen());

        Button addButton = new Button();
        addButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.PLUS).build());
        addButton.setTooltip(new Tooltip("Add new switch"));
        addButton.setPrefWidth(35);

        addButton.setOnAction(event -> {
            Switch newSwitch = Switch.create(Switch.class);
            newSwitch.setTarget("target");
            newSwitch.setEnabled(false);
            newSwitch.setParent(this);
            newSwitch.save();

            addSwitch(newSwitch);

            // Add the new switch just above the plus button
            switchRows.getChildren().add(switchRows.getChildren().size() - 1, createSwitchNodeRow(newSwitch));
        });

        addSwitchRow.getChildren().add(addButton);

        return addSwitchRow;
    }

    public Switch getSwitchByValue(String value) {
        if (value != null) {
            for (Switch aSwitch : getSwitches()) {
                if (value.equals(aSwitch.getTarget())) {
                    return aSwitch;
                }
            }
        }

        return null;
    }
}
