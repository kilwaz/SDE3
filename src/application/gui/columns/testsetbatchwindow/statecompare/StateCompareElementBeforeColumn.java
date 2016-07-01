package application.gui.columns.testsetbatchwindow.statecompare;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class StateCompareElementBeforeColumn extends TableColumn<CompareStateElementObject, String> {
    public StateCompareElementBeforeColumn() {
        setText("Before");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("Before"));
    }
}