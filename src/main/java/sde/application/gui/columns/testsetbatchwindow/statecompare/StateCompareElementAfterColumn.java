package sde.application.gui.columns.testsetbatchwindow.statecompare;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class StateCompareElementAfterColumn extends TableColumn<CompareStateElementObject, Object> {
    public StateCompareElementAfterColumn() {
        setText("After");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("After"));
        setCellFactory(column -> new StateCompareElementCell());
    }
}