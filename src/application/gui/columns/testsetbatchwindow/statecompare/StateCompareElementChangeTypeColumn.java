package application.gui.columns.testsetbatchwindow.statecompare;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;

public class StateCompareElementChangeTypeColumn extends TableColumn<CompareStateElementObject, Object> {
    public StateCompareElementChangeTypeColumn() {
        setText("Change Type");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("ChangeType"));
        setCellFactory(column -> new StateCompareElementCell());
    }
}

