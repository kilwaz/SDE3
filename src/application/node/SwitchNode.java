package application.node;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.Program;
import application.gui.Switch;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.textfield.TextFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SwitchNode extends DrawableNode {
    private List<Switch> aSwitches = new ArrayList<>();
    private Color fillColour = Color.LIGHTCYAN;

    private VBox switchRows;

    // This will make a copy of the node passed to it
    public SwitchNode(SwitchNode switchNode) {
        this.setId(-1);
        this.setX(switchNode.getX());
        this.setY(switchNode.getY());
        this.setWidth(switchNode.getWidth());
        this.setHeight(switchNode.getHeight());
        this.setColor(switchNode.getColor());
        this.setScale(switchNode.getScale());
        this.setContainedText(switchNode.getContainedText());
        this.setProgramId(switchNode.getProgramId());
        this.setNextNodeToRun(switchNode.getNextNodeToRun());

        // This copies all of the switches and creates new object for each one using the copy constructor
        aSwitches.addAll(switchNode.getSwitches().stream().map(loopSwitch -> new Switch(loopSwitch, this)).collect(Collectors.toList()));
    }

    public SwitchNode(Integer id, Integer programId) {
        super(id, programId);
        DataBank.loadSwitches(this);
    }

    public SwitchNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        DataBank.loadSwitches(this);
    }

    public void updateSwitchTarget(Integer switchId, String target) {
        for (Switch aSwitch : aSwitches) {
            if (aSwitch.getId().equals(switchId)) {
                aSwitch.setTarget(target);
                DataBank.saveSwitch(aSwitch);
            }
        }
    }

    public void updateSwitchEnabled(Integer switchId, Boolean enabled) {
        for (Switch aSwitch : aSwitches) {
            if (aSwitch.getId().equals(switchId)) {
                aSwitch.setEnabled(enabled);
                DataBank.saveSwitch(aSwitch);
            }
        }
    }

    public void setSwitches(List<Switch> aSwitches) {
        this.aSwitches = aSwitches;
    }

    public List<Switch> getSwitches() {
        return this.aSwitches;
    }

    public Switch getSwitch(Integer id) {
        for (Switch aSwitch : aSwitches) {
            if (aSwitch.getId().equals(id)) {
                return aSwitch;
            }
        }

        return null;
    }

    public void removeSwitch(Switch switchObj) {
        Switch switchToRemove = null;
        for (Switch aSwitch : aSwitches) {
            if (aSwitch.getId().equals(switchObj.getId())) {
                switchToRemove = aSwitch;
            }
        }

        if (switchToRemove != null) {
            aSwitches.remove(switchToRemove);
        } else {
            System.out.println("Cannot find switch to remove with id " + switchObj.getId());
        }
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<SavableAttribute>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void addSwitch(Switch aSwitch) {
        aSwitches.add(aSwitch);
    }

    public void run(Boolean whileWaiting, HashMap<String, Object> map) {
        for (Switch aSwitch : aSwitches) {
            if (aSwitch.isEnabled()) {
                Program.runHelper(aSwitch.getTarget(), DataBank.currentlyEditProgram.getFlowController().getReferenceID(), whileWaiting, false, map);
            }
        }
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        // The ordering here is Tab < ScrollPane < AnchorPane
        Tab tab = controller.createDefaultNodeTab(this);
        ScrollPane scrollPane = new ScrollPane();
        AnchorPane anchorPane = (AnchorPane) tab.getContent(); // We get the Anchor pane from the default Tab and change it to a ScrollPane

        scrollPane.setContent(anchorPane);

        switchRows = new VBox(5);
        switchRows.setLayoutY(55);
        switchRows.setLayoutX(11);

        List<Switch> aSwitches = getSwitches();
        for (Switch aSwitch : aSwitches) {
            switchRows.getChildren().add(createSwitchNodeRow(aSwitch));
        }

        switchRows.getChildren().add(createAddSwitchNodeRow());

        anchorPane.getChildren().add(switchRows);
        tab.setContent(scrollPane);

        // Go back to the beginning and run the code to show the tab, it should now exist
        return tab;
    }

    public HBox createSwitchNodeRow(Switch aSwitch) {
        HBox switchRow = new HBox(5);
        switchRow.setId("switchRow-" + aSwitch.getId() + "-" + getId());

        // Tick / Cross button
        ToggleButton firstSwitchButton = AwesomeDude.createIconToggleButton(AwesomeIcon.CLOSE, "", "12", null);
        if (aSwitch.isEnabled()) {
            AwesomeDude.setIcon(firstSwitchButton, AwesomeIcon.CHECK);
            firstSwitchButton.setSelected(true);
        } else {
            AwesomeDude.setIcon(firstSwitchButton, AwesomeIcon.CLOSE);
            firstSwitchButton.setSelected(false);
        }
        firstSwitchButton.setPrefWidth(35);
        firstSwitchButton.setId("switchButton-" + aSwitch.getId() + "-" + getId());
        firstSwitchButton.setOnAction(event -> {
            ToggleButton toggleButton = (ToggleButton) event.getSource();
            Program program = DataBank.currentlyEditProgram;
            String[] fieldId = toggleButton.getId().split("-");
            SwitchNode switchNode = (SwitchNode) program.getFlowController().getNodeById(Integer.parseInt(fieldId[2]));

            if (toggleButton.isSelected()) {
                AwesomeDude.setIcon(toggleButton, AwesomeIcon.CHECK);
                toggleButton.setSelected(true);
                switchNode.updateSwitchEnabled(Integer.parseInt(fieldId[1]), true);
            } else {
                AwesomeDude.setIcon(toggleButton, AwesomeIcon.CLOSE);
                toggleButton.setSelected(false);
                switchNode.updateSwitchEnabled(Integer.parseInt(fieldId[1]), false);
            }

            program.getFlowController().checkConnections(); // Toggling a switch will make or break connections
        });
        switchRow.getChildren().add(firstSwitchButton);

        // Text field
        TextField switchField = TextFields.createClearableTextField();
        //ValidationSupport validationSupport = new ValidationSupport();
        //validationSupport.registerValidator(switchField, Validator.createEqualsValidator("Node name does not exist", "test");
        //validationSupport.registerValidator(switchField, Validator.createEmptyValidator("SHOULD NOT BE EMPTY"));

        switchField.setText(aSwitch.getTarget()); // The text of the field should be set before linking it to auto complete to avoid jittery UI
        TextFields.bindAutoCompletion(switchField, DataBank.currentlyEditProgram.getFlowController().getNodes());

        switchField.setId("switchField-" + aSwitch.getId() + "-" + getId());
        switchField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            Program program = DataBank.currentlyEditProgram;
            String[] fieldId = textField.getId().split("-");
            SwitchNode switchNode = (SwitchNode) program.getFlowController().getNodeById(Integer.parseInt(fieldId[2]));
            if (!textField.getText().isEmpty()) {
                switchNode.updateSwitchTarget(Integer.parseInt(fieldId[1]), textField.getText());

                program.getFlowController().checkConnections(); // Renaming a node might make or break connections

                DataBank.saveNode(switchNode);
            } else {
                Switch switchToRemove = switchNode.getSwitch(Integer.parseInt(fieldId[1]));

                DataBank.deleteSwitch(switchToRemove);
                removeSwitch(switchToRemove);

                Node rowToRemove = null;
                for (Node node : switchRows.getChildren()) {
                    if (node.getId().equals("switchRow-" + switchToRemove.getId() + "-" + getId())) {
                        rowToRemove = node;
                    }
                }

                if (rowToRemove != null) {
                    switchRows.getChildren().remove(rowToRemove);
                }

                program.getFlowController().checkConnections();
            }

            Controller.getInstance().updateCanvasControllerNow();
        });

        switchRow.getChildren().add(switchField);

        return switchRow;
    }

    public HBox createAddSwitchNodeRow() {
        HBox addSwitchRow = new HBox(5);
        addSwitchRow.setId("addSwitchRow-" + getId());

        Button addButton = AwesomeDude.createIconButton(AwesomeIcon.PLUS);
        addButton.setPrefWidth(35);

        addButton.setOnAction(event -> {
            Switch newSwitch = DataBank.createNewSwitch("target", this, false);

            // Add the new switch just above the plus button
            switchRows.getChildren().add(switchRows.getChildren().size() - 1, createSwitchNodeRow(newSwitch));
        });

        addSwitchRow.getChildren().add(addButton);

        return addSwitchRow;
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
