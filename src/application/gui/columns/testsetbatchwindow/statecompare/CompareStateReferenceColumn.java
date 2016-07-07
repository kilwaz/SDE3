package application.gui.columns.testsetbatchwindow.statecompare;

import application.test.PageStateCompare;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;

public class CompareStateReferenceColumn extends TableColumn<PageStateCompare, String> {
    public CompareStateReferenceColumn() {
        setText("Reference");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("Reference"));
    }
}
