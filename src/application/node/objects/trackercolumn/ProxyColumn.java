package application.node.objects.trackercolumn;

import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProxyColumn extends TableColumn {
    public ProxyColumn() {
        setText("Proxy");
        setPrefWidth(100);
        setCellValueFactory(new PropertyValueFactory<RecordedRequest, String>("ProxyConnectionString"));
    }
}
