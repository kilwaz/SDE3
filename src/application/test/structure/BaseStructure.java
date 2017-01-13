package application.test.structure;

import application.test.TestCommand;
import application.utils.AppParams;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;

public class BaseStructure {
    public static final Integer STRUCTURE_CLICK = 0;
    public static final Integer STRUCTURE_URL = 1;
    private static HashMap<Integer, String> availableStructures = new HashMap<>();
    private static ObservableList<String> availableStructureNames = FXCollections.observableArrayList();

    static {
        availableStructures.put(0, "Click");
        availableStructures.put(1, "URL");

        availableStructureNames.addAll(availableStructures.values());
    }

    private Integer structureType;
    private Integer lineNumber;

    public BaseStructure(TestCommand testCommand) {
        this.lineNumber = testCommand.getCommandLineNumber();
    }

    public String getStructureName() {
        return availableStructures.get(structureType);
    }

    public int getStructureType() {
        return structureType;
    }

    public void setStructureType(int structureType) {
        this.structureType = structureType;
    }

    public String toScript() {
        return "Missing";
    }

    public Node createSupportingInterface() {
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPrefHeight(50d);
        anchorPane.setMinHeight(50d);
        anchorPane.setMaxHeight(50d);
        anchorPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5.0), BorderWidths.DEFAULT)));
        anchorPane.setStyle("-fx-background-color: rgba(0,0,0,0.75),\n" +
                "                           rgba(255,255,255,0.75),\n" +
                "                           linear-gradient(to bottom,#dddddd 0%,#eeeeee 100%);\n");

        HBox hBox = new HBox(5);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(0, 0, 0, 5));

        Label hashNumber = new Label("#" + getLineNumber());
        hashNumber.setFont(AppParams.getFont(15));

        ChoiceBox<String> cb = new ChoiceBox<>(availableStructureNames);
        cb.getSelectionModel().select(getStructureName());

        Separator vSeparator = new Separator();
        vSeparator.setPrefHeight(50d);
        vSeparator.setOrientation(Orientation.VERTICAL);

        hBox.getChildren().addAll(hashNumber, vSeparator, cb, createStructureInterface());

        anchorPane.getChildren().add(hBox);
        return anchorPane;
    }

    // Needs to be overridden by structure classes and has specific UI elements for that structure
    public Node createStructureInterface() {
        return null;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}