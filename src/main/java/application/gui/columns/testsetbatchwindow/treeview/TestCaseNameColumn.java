package application.gui.columns.testsetbatchwindow.treeview;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TreeTableColumn;

public class TestCaseNameColumn extends TreeTableColumn<TestCaseTreeObject, String> {
    public TestCaseNameColumn() {
        setText("Tests");
        setPrefWidth(130);
        setCellValueFactory((TreeTableColumn.CellDataFeatures<TestCaseTreeObject, String> p) -> new ReadOnlyStringWrapper(p.getValue().getValue().getName()));
    }
}
