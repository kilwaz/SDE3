package sde.application.test.structure;

import sde.application.gui.Controller;
import sde.application.test.TestCommand;
import sde.application.test.core.TestStructure;
import sde.application.utils.AppParams;
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
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;

public class BaseStructure {
    public static final Integer STRUCTURE_CLICK = 0;
    public static final Integer STRUCTURE_URL = 1;
    private static HashMap<Integer, String> availableStructures = new HashMap<>();
    private static ObservableList<String> availableStructureNames = FXCollections.observableArrayList();

    private static Logger log = Logger.getLogger(BaseStructure.class);

    private static Boolean draggingStructure = false;
    private static Double dragStartY = 0d;
    private static Double baseHeight = 50d;

    static {
        availableStructures.put(0, "Click");
        availableStructures.put(1, "URL");

        availableStructureNames.addAll(availableStructures.values());
    }

    private TestStructure parentStructure;
    private Integer structureType;
    private Integer lineNumber;

    public BaseStructure(TestCommand testCommand, TestStructure parentStructure) {
        this.lineNumber = testCommand.getCommandLineNumber();
        this.parentStructure = parentStructure;
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
        anchorPane.setPrefHeight(baseHeight);
        anchorPane.setMinHeight(baseHeight);
        anchorPane.setMaxHeight(baseHeight);
        anchorPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5.0), BorderWidths.DEFAULT)));
        anchorPane.setStyle("-fx-background-color: rgba(0,0,0,0.75),\n" +
                "                           rgba(255,255,255,0.75),\n" +
                "                           linear-gradient(to bottom,#dddddd 0%,#eeeeee 100%);\n");

        HBox hBox = new HBox(5);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(0, 0, 0, 5));

        Label hashNumber = new Label("#" + getLineNumber());
        hashNumber.setId("rowLineNumber" + getLineNumber());
        hashNumber.setFont(AppParams.getFont(15));

        ChoiceBox<String> cb = new ChoiceBox<>(availableStructureNames);
        cb.getSelectionModel().select(getStructureName());

        Separator vSeparator = new Separator();
        vSeparator.setPrefHeight(baseHeight);
        vSeparator.setOrientation(Orientation.VERTICAL);

        hBox.getChildren().addAll(hashNumber, vSeparator, cb, createStructureInterface());

        anchorPane.setOnMousePressed(event -> {
            draggingStructure = true;
            dragStartY = event.getSceneY();
            Controller.getInstance().setCursor(javafx.scene.Cursor.MOVE);
        });

//        anchorPane.setOnMouseDragged(event -> {
//            Double thisY = 10.0 + ((lineNumber - 1) * (baseHeight + 5));
//
//            Double diffY = event.getSceneY() - dragStartY;
//            Long lineDifference = Math.round(diffY / (baseHeight + 5));
//
//            log.info("Diff: " + diffY + " " + lineDifference + " - " + event.getY());
//        });

        anchorPane.setOnMouseReleased(event -> {
            VBox parentVBox = (VBox) anchorPane.getParent();

            draggingStructure = false;
            Controller.getInstance().setCursor(javafx.scene.Cursor.DEFAULT);

            Double diffY = event.getSceneY() - dragStartY;
            Long lineDifference = Math.round(diffY / (baseHeight + 5));

            Integer draggedLine = lineNumber;
            Integer draggedOnLine = lineNumber + new Long(Math.round(diffY / (baseHeight + 5))).intValue();

            if (draggedOnLine < 1) {
                draggedOnLine = 1;
            }

            parentStructure.swapBaseStructures(draggedLine, draggedOnLine);

            // Corrects the # text numbers at the start of each row
            Label draggedLineLabel = (Label) parentVBox.lookup("#rowLineNumber" + draggedLine);
            Label draggedOnLineLabel = (Label) parentVBox.lookup("#rowLineNumber" + draggedOnLine);

            draggedLineLabel.setText("#" + draggedOnLine);
            draggedLineLabel.setId("rowLineNumber" + draggedOnLine);
            draggedOnLineLabel.setText("#" + draggedLine);
            draggedOnLineLabel.setId("rowLineNumber" + draggedLine);

            // Swap the rows over
            ObservableList<Node> workingCollection = FXCollections.observableArrayList(parentVBox.getChildren());
            Collections.swap(workingCollection, draggedLine - 1, draggedOnLine - 1);
            parentVBox.getChildren().setAll(workingCollection);
        });

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

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
}