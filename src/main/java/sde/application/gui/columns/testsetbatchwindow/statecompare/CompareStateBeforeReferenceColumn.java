package sde.application.gui.columns.testsetbatchwindow.statecompare;

import sde.application.test.PageStateCompare;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class CompareStateBeforeReferenceColumn extends TableColumn<PageStateCompare, String> {
    public CompareStateBeforeReferenceColumn() {
        setText("Before Ref");
        setPrefWidth(120);
        setCellValueFactory(new PropertyValueFactory<>("beforeStateName"));
    }
}
