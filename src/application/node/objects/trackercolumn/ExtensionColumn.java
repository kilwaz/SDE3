package application.node.objects.trackercolumn;

import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class ExtensionColumn extends TableColumn {
    public ExtensionColumn() {
        setText("Extension");
        setPrefWidth(70);
        setCellValueFactory(new PropertyValueFactory<RecordedRequest, String>("Extension"));
    }
}
