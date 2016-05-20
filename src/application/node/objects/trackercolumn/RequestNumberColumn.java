package application.node.objects.trackercolumn;

import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class RequestNumberColumn extends TableColumn {
    public RequestNumberColumn() {
        setText("#");
        setPrefWidth(30);
        setCellValueFactory(new PropertyValueFactory<RecordedRequest, Integer>("RequestNumber"));
    }
}
