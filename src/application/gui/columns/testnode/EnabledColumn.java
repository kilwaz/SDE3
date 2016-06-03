package application.gui.columns.testnode;

import application.node.implementations.TestNode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class EnabledColumn extends TableColumn {
    public EnabledColumn() {
        setText("Enabled");
        setPrefWidth(130);
        setCellValueFactory(new PropertyValueFactory<TestNode, String>("ContainedText"));
    }
}
