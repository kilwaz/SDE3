package sde.application.gui.columns.requesttracker;

import sde.application.net.proxy.MetaRecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProxyColumn extends TableColumn {
    public ProxyColumn() {
        setText("Proxy");
        setPrefWidth(100);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, String>("ProxyConnectionString"));
    }
}
