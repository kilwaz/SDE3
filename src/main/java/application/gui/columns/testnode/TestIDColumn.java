package application.gui.columns.testnode;

import javafx.scene.control.TreeTableColumn;

public class TestIDColumn extends TreeTableColumn<LinkedTestCaseTreeObject, String> {
    public TestIDColumn() {
        setText("Test ID");
        setPrefWidth(130);
//        setCellValueFactory((TreeTableColumn.CellDataFeatures<LinkedTestCaseTreeObject, String> p) -> new ReadOnlyStringWrapper(p.getValue().getValue().getLinkedTestCase()));
    }
}
