package application.node.objects.trackercolumn;

import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class URLColumn extends TableColumn {
    public URLColumn() {
        setText("URL");
        setPrefWidth(200);
        setCellValueFactory(new PropertyValueFactory<RecordedRequest, String>("LocalUrl"));
    }
}
