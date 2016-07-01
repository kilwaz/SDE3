package application.gui.columns.testsetbatchwindow.statecompare;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class StateCompareElementAfterColumn extends TableColumn<CompareStateElementObject, String> {
    public StateCompareElementAfterColumn() {
        setText("After");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("After"));
    }
}