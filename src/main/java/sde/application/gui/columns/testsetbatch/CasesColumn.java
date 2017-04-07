package sde.application.gui.columns.testsetbatch;

import sde.application.test.core.TestSetBatch;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class CasesColumn extends TableColumn<TestSetBatch, Integer> {
    public CasesColumn() {
        setText("Cases");
        setPrefWidth(50);
        setCellValueFactory(new PropertyValueFactory<>("caseCount"));
    }
}
