package application.gui.columns.requesttracker;

import application.net.proxy.MetaRecordedRequest;
import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class IPColumn extends TableColumn {
    public IPColumn() {
        setText("IP");
        setPrefWidth(80);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, String>("IP"));
    }
}
