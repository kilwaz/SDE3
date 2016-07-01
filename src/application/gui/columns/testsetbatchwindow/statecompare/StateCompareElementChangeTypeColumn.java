package application.gui.columns.testsetbatchwindow.statecompare;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class StateCompareElementChangeTypeColumn extends TableColumn<CompareStateElementObject, String> {
    public StateCompareElementChangeTypeColumn() {
        setText("Change Type");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("ChangeType"));
    }
}

