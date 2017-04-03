package sde.application.gui.columns.testsetbatchwindow.statecompare;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class StateCompareElementAttributeNameColumn extends TableColumn<CompareStateElementObject, Object> {
    public StateCompareElementAttributeNameColumn() {
        setText("Attribute Name");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("AttributeName"));
        setCellFactory(column -> new StateCompareElementCell());
    }
}
