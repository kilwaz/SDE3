package application.test.structure;

import application.test.TestCommand;
import application.utils.AppParams;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
                "                           linear-gradient(to bottom,#dddddd 0%,#eeeeee 100%);\n" +
                " ");
//                " -fx-background-insets: 0 1 1 0, 1 0 0 1, 1;");

        VBox vBox = new VBox(5);
        HBox hBox = new HBox(5);
        hBox.setAlignment(Pos.BASELINE_CENTER);

        Label hashNumber = new Label("#" + getLineNumber());
        hashNumber.setFont(AppParams.getFont(15));
        //hashNumber.setPrefHeight(50d);
        //hashNumber.setAlignment(Pos.CENTER_LEFT);

//        Label commandLabel = new Label("Command:");
//        commandLabel.setFont(AppParams.getFont(12));

        ChoiceBox<String> cb = new ChoiceBox<>(availableStructureNames);
        cb.getSelectionModel().select(getStructureName());

        Separator vSeparator = new Separator();
        vSeparator.setPrefHeight(50d);
        vSeparator.setOrientation(Orientation.VERTICAL);

        hBox.getChildren().addAll(hashNumber,vSeparator,cb, createStructureInterface());
        //vBox.getChildren().addAll(hashNumber, vSeparator, hBox);

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