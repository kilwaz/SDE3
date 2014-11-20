package application.node;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.Program;
import application.gui.Switch;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
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
        if (aSwitches.size() < 4) {
            DataBank.createNewSwitch("target", this, true);
        }
    }

    public SwitchNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        DataBank.loadSwitches(this);
    }

    public void updateSplitTarget(Integer splitId, String target) {
        for (Switch aSwitch : aSwitches) {
            if (aSwitch.getId().equals(splitId)) {
                aSwitch.setTarget(target);
                DataBank.saveSwitch(aSwitch);
            }
        }

    }

    public void updateSplitEnabled(Integer splitId, Boolean enabled) {
        for (Switch aSwitch : aSwitches) {
            if (aSwitch.getId().equals(splitId)) {
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

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        List<Switch> aSwitches = getSwitches();
        for (Switch aSwitch : aSwitches) {
            rows.getChildren().add(createSwitchNodeRow(aSwitch, getId()));
        }

        anchorPane.getChildren().add(rows);

        // Go back to the beginning and run the code to show the tab, it should now exist
        return tab;
    }

    public HBox createSwitchNodeRow(Switch aSwitch, Integer splitId) {
        HBox row = new HBox(5);
        Button firstSplitButton = new Button();
        if (aSwitch.isEnabled()) {
            firstSplitButton.setText("Enabled");
        } else {
            firstSplitButton.setText("Disabled");
        }
        firstSplitButton.setPrefWidth(80);
        firstSplitButton.setId("switchButton-" + aSwitch.getId() + "-" + splitId);
        firstSplitButton.setOnAction(event -> {
            Button button = (Button) event.getSource();
            Program program = DataBank.currentlyEditProgram;
            String[] fieldId = button.getId().split("-");
            SwitchNode switchNode = (SwitchNode) program.getFlowController().getNodeById(Integer.parseInt(fieldId[2]));

            if ("Disabled".equals(button.getText())) {
                button.setText("Enabled");
                switchNode.updateSplitEnabled(Integer.parseInt(fieldId[1]), true);
            } else {
                button.setText("Disabled");
                switchNode.updateSplitEnabled(Integer.parseInt(fieldId[1]), false);
            }

            program.getFlowController().checkConnections(); // Toggling a switch will make or break connections
        });
        row.getChildren().add(firstSplitButton);

        TextField switchField = TextFields.createClearableTextField();
        TextFields.bindAutoCompletion(switchField, DataBank.currentlyEditProgram.getFlowController().getNodes());
        //ValidationSupport validationSupport = new ValidationSupport();
        //validationSupport.registerValidator(switchField, Validator.createEqualsValidator("Node name does not exist", "test");
        //validationSupport.registerValidator(switchField, Validator.createEmptyValidator("SHOULD NOT BE EMPTY"));

        switchField.setText(aSwitch.getTarget());
        switchField.setId("switchField-" + aSwitch.getId() + "-" + splitId);
        switchField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                Program program = DataBank.currentlyEditProgram;
                String[] fieldId = textField.getId().split("-");
                SwitchNode switchNode = (SwitchNode) program.getFlowController().getNodeById(Integer.parseInt(fieldId[2]));
                switchNode.updateSplitTarget(Integer.parseInt(fieldId[1]), textField.getText());

                program.getFlowController().checkConnections(); // Renaming a node might make or break connections

                DataBank.saveNode(switchNode);
                Controller.getInstance().updateCanvasControllerNow();
            }
        });

        row.getChildren().add(switchField);

        return row;
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
