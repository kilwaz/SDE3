package application.gui.columns.testsetbatch;

import application.test.core.TestSetBatch;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class CasesColumn extends TableColumn {
    public CasesColumn() {
        setText("Cases");
        setPrefWidth(50);
        setCellValueFactory(new PropertyValueFactory<TestSetBatch, Integer>("caseCount"));
    }
}
