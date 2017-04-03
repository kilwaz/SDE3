package application.gui.columns.testsetbatchwindow.statecompare;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class StateCompareElementInitialRefColumn extends TableColumn<CompareStateElementObject, Object> {
    public StateCompareElementInitialRefColumn() {
        setText("Initial Ref");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("InitialRef"));
        setCellFactory(column -> new StateCompareElementCell());
    }
}