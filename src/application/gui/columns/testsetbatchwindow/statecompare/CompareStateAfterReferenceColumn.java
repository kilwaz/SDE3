package application.gui.columns.testsetbatchwindow.statecompare;


import application.test.PageStateCompare;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class CompareStateAfterReferenceColumn extends TableColumn<PageStateCompare, String> {
    public CompareStateAfterReferenceColumn() {
        setText("After Ref");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("afterStateName"));
    }
}
