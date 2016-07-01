package application.gui.columns.testsetbatchwindow.statecompare;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class StateCompareElementMatchedColumn extends TableColumn<CompareStateElementObject, Boolean> {
    public StateCompareElementMatchedColumn() {
        setText("Match");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("Matched"));
    }
}
