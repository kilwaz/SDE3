package sde.application.gui.columns.testsetbatchwindow.statecompare;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class StateCompareElementIncreasedByColumn extends TableColumn<CompareStateElementObject, Object> {
    public StateCompareElementIncreasedByColumn() {
        setText("Increased By");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("IncreasedBy"));
        setCellFactory(column -> new StateCompareElementCell());
    }
}