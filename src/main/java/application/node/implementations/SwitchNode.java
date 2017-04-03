package application.node.implementations;

import application.data.SavableAttribute;
import application.data.model.dao.SwitchDAO;
import application.gui.Controller;
import application.gui.GUIUpdate;
import application.gui.Program;
import application.gui.update.switchnode.AddSwitchRow;
import application.gui.update.switchnode.RemoveSwitchRow;
import application.gui.update.switchnode.ToggleButtonImage;
import application.node.design.DrawableNode;
import application.node.objects.Switch;
import application.node.objects.comparators.SwitchTargetComparator;
import application.utils.NodeRunParams;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import org.controlsfx.control.textfield.TextFields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SwitchNode extends DrawableNode {
    private static Logger log = Logger.getLogger(SwitchNode.class);
    private List<Switch> aSwitches = null;
    private HashMap<Switch, HBox> switchUI = new HashMap<>();
    private VBox switchRows;
    private ContextMenu programListContextMenu;
    private Boolean listIsSorted = false;
    private Boolean listIsDescSorted = false;
    private SwitchNode instance;
    private AnchorPane anchorPane;
    private VBox rows;

    private static final String SORT_NONE = "None";
    private static final String SORT_ASCENDING = "Ascending";
    private static final String SORT_DESCENDING = "Descending";

    // This will make a copy of the node passed to it
    public SwitchNode(SwitchNode switchNode) {
        this.setX(switchNode.getX());
        this.setY(switchNode.getY());
        this.setWidth(switchNode.getWidth());
        this.setHeight(switchNode.getHeight());
        this.setColor(switchNode.getColor());
        this.setScale(switchNode.getScale());
        this.setContainedText(switchNode.getContainedText());
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

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        SavableAttribute listIsSortedAttribute = SavableAttribute.create(SavableAttribute.class);
        listIsSortedAttribute.init("ListIsSorted", listIsSorted.getClass().getName(), listIsSorted, this);
        savableAttributes.add(listIsSortedAttribute);

        SavableAttribute listIsDescSortedAttribute = SavableAttribute.create(SavableAttribute.class);
        listIsDescSortedAttribute.init("ListIsDescSorted", listIsDescSorted.getClass().getName(), listIsDescSorted, this);
        savableAttributes.add(listIsDescSortedAttribute);

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void addSwitch(Switch aSwitch) {
        getSwitches().add(aSwitch);
        aSwitch.setParent(this);
        if (isVisible()) {
            createHBoxUI(aSwitch);
        }
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        for (Switch aSwitch : getSwitches()) {
            if (aSwitch.isEnabled()) {
                Program.runHelper(aSwitch.getTarget(), getProgram().getFlowController().getReferenceID(), this, whileWaiting, true, null, nodeRunParams);
            }
        }
    }

    private void createHBoxUI(Switch aSwitch) {
        HBox hbox = createSwitchNodeRow(aSwitch);
        GUIUpdate.update(new AddSwitchRow(switchRows, hbox));
        switchUI.put(aSwitch, hbox);
    }

    public Tab createInterface() {
        instance = this;

        Controller controller = Controller.getInstance();
        switchUI.clear(); // Remove any previous UI elements

        Tab tab = controller.createDefaultNodeTab(this);
        anchorPane = controller.getContentAnchorPaneOfTab(tab);

        // Hide the context menu if it is showing
        anchorPane.setOnMouseClicked(event -> {
            if (programListContextMenu != null) {
                programListContextMenu.hide();
            }
        });

        // Construct the context menu
        anchorPane.setOnContextMenuRequested(event -> {
            if (programListContextMenu != null) {
                programListContextMenu.hide();
            }

            MenuItem menuItemEnableAll = new MenuItem("Enabled all");
            menuItemEnableAll.setOnAction(event1 -> {
                for (Switch aSwitch : aSwitches) {
                    aSwitch.setEnabled(true);
                    aSwitch.save();
                }
            });

            MenuItem menuItemDisableAll = new MenuItem("Disable all");
            menuItemDisableAll.setOnAction(event1 -> {
                for (Switch aSwitch : aSwitches) {
                    aSwitch.setEnabled(false);
                    aSwitch.save();
                }
            });

            programListContextMenu = new ContextMenu();
            programListContextMenu.getItems().add(menuItemEnableAll);
            programListContextMenu.getItems().add(menuItemDisableAll);
            programListContextMenu.show(anchorPane, event.getScreenX(), event.getScreenY());
        });

        buildEntireSwitchNodeGUI();

        // Go back to the beginning and run the code to show the tab, it should now exist
        return tab;
    }

    public HBox createSortRow() {
        // Choose a display order for the switches
        Label sortLabel = new Label("Sort Order:");

        HBox sortHBox = new HBox(5);
        ChoiceBox<String> startWhenChoice = new ChoiceBox<>();

        List<String> sortedChoiceList = new ArrayList<>();
        sortedChoiceList.add(SORT_NONE);
        sortedChoiceList.add(SORT_ASCENDING);
        sortedChoiceList.add(SORT_DESCENDING);

        startWhenChoice.setItems(FXCollections.observableList(sortedChoiceList));
        if (!listIsSorted) {
            startWhenChoice.setValue(SORT_NONE);
        } else {
            if (listIsDescSorted) {
                startWhenChoice.setValue(SORT_DESCENDING);
            } else {
                startWhenChoice.setValue(SORT_ASCENDING);
            }
        }
        startWhenChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (SORT_NONE.equals(newValue)) {
                listIsSorted = false;
            } else if (SORT_ASCENDING.equals(newValue)) {
                listIsSorted = true;
                listIsDescSorted = false;
                buildEntireSwitchNodeGUI();
            } else if (SORT_DESCENDING.equals(newValue)) {
                listIsSorted = true;
                listIsDescSorted = true;
                buildEntireSwitchNodeGUI();
            }

            instance.save();
        });

        sortHBox.getChildren().addAll(sortLabel, startWhenChoice);
        return sortHBox;
    }

    public void buildEntireSwitchNodeGUI() {
        switchUI.clear();
        switchRows = new VBox(5);

        List<Switch> switches = getSwitches();
        switches.sort(new SwitchTargetComparator());
        if (listIsDescSorted) {
            Collections.reverse(switches);
        }
        switches.forEach(this::createHBoxUI);

        VBox newRows = new VBox(5);
        newRows.setLayoutY(55);
        newRows.setLayoutX(11);

        newRows.getChildren().add(createSortRow());
        newRows.getChildren().add(switchRows);
        newRows.getChildren().add(createAddSwitchNodeRow());

        anchorPane.getChildren().remove(rows);
        anchorPane.getChildren().add(newRows);
        rows = newRows;
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
            deleteSwitch(switchNode.getSwitch(fieldId[1]));
        });
        switchRow.getChildren().add(deleteSwitch);

        // Tick / Cross button
        ToggleButton firstSwitchButton = new ToggleButton();
        firstSwitchButton.selectedProperty().bindBidirectional(aSwitch.isEnabledProp());
        firstSwitchButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            GUIUpdate.update(new ToggleButtonImage(firstSwitchButton));
        });

        GUIUpdate.update(new ToggleButtonImage(firstSwitchButton));

        firstSwitchButton.setPrefWidth(35);
        firstSwitchButton.setId("switchButton-" + aSwitch.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen());
        firstSwitchButton.setOnAction(event -> {
            ToggleButton toggleButton = (ToggleButton) event.getSource();
            String[] fieldId = toggleButton.getId().split("-");
            SwitchNode switchNode = (SwitchNode) getProgram().getFlowController().getNodeByUuidWithoutHyphen(fieldId[2]);

            if (toggleButton.isSelected()) {
                switchNode.updateSwitchEnabled(fieldId[1], true);
            } else {
                switchNode.updateSwitchEnabled(fieldId[1], false);
            }

            getProgram().getFlowController().checkConnections(); // Toggling a switch will make or break connections
        });
        switchRow.getChildren().add(firstSwitchButton);

        // Text field
        TextField switchField = TextFields.createClearableTextField();
        switchField.textProperty().bindBidirectional(aSwitch.getTargetProp());
        //TextFields.bindAutoCompletion(switchField, getProgram().getFlowController().getNodes());

        switchField.setId("switchField-" + aSwitch.getUuidStringWithoutHyphen() + "-" + getUuidStringWithoutHyphen());
        switchField.setOnKeyReleased(event -> {
            TextField textField = (TextField) event.getSource();
            String[] fieldId = textField.getId().split("-");
            SwitchNode switchNode = (SwitchNode) getProgram().getFlowController().getNodeByUuidWithoutHyphen(fieldId[2]);
            if (!textField.getText().isEmpty()) {
                switchNode.updateSwitchTarget(fieldId[1], textField.getText());

                getProgram().getFlowController().checkConnections(); // Renaming a node might make or break connections

                switchNode.save();
            }

            Controller.getInstance().updateCanvasControllerNow();
        });

        switchField.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (!newPropertyValue) { // On lose focus
                if (listIsSorted) {
                    buildEntireSwitchNodeGUI();
                }
            }
        });

        switchRow.getChildren().add(switchField);

        return switchRow;
    }

    public void deleteSwitch(Switch switchToRemove) {
        if (switchToRemove != null) {
            GUIUpdate.update(new RemoveSwitchRow(switchRows, switchToRemove));
            getSwitches().remove(switchToRemove);
            switchToRemove.delete();
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

    public Boolean getListIsSorted() {
        return listIsSorted;
    }

    public void setListIsSorted(Boolean listIsSorted) {
        this.listIsSorted = listIsSorted;
    }

    public Boolean getListIsDescSorted() {
        return listIsDescSorted;
    }

    public void setListIsDescSorted(Boolean listIsDescSorted) {
        this.listIsDescSorted = listIsDescSorted;
    }
}
