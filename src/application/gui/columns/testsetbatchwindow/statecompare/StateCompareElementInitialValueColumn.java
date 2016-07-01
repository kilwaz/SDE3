package application.gui.columns.testsetbatchwindow.statecompare;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class StateCompareElementInitialValueColumn extends TableColumn<CompareStateElementObject, String> {
    public StateCompareElementInitialValueColumn() {
        setText("Initial Value");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("InitialValue"));
    }
}