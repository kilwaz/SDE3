package application.gui.columns.testsetbatchwindow.statecompare;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class StateCompareElementFinalValueColumn extends TableColumn<CompareStateElementObject, String> {
    public StateCompareElementFinalValueColumn() {
        setText("Final Value");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("FinalValue"));
    }
}