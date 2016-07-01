package application.gui.columns.testnode;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

public class NodeNameColumn extends TreeTableColumn<LinkedTestCaseTreeObject, String> {
    public NodeNameColumn() {
        setText("Test Name");
        setPrefWidth(130);
        setCellValueFactory((TreeTableColumn.CellDataFeatures<LinkedTestCaseTreeObject, String> p) -> new ReadOnlyStringWrapper(p.getValue().getValue().getName()));

//        setCellFactory(new Callback<TreeTableColumn<LinkedTestCaseTreeObject, String>, TreeTableCell<LinkedTestCaseTreeObject, String>>() {
//            @Override
//            public TreeTableCell<LinkedTestCaseTreeObject, String> call(TreeTableColumn<LinkedTestCaseTreeObject, String> param) {
//                TreeTableCell<LinkedTestCaseTreeObject, String> cell = new TreeTableCell<LinkedTestCaseTreeObject, String>() {
//                    @Override
//                    public void updateItem(String item, boolean empty) {
//                        super.updateItem(item, empty);
//                        if (item != null) {
//                            setText(item);
//                        }
//                    }
//                };
//                cell.setAlignment(Pos.CENTER);
//                return cell;
//            }
//        });
    }
}
