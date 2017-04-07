package sde.application.gui.columns.requesttracker;

import sde.application.net.proxy.MetaRecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class HostColumn extends TableColumn {
    public HostColumn() {
        setText("Host");
        setPrefWidth(200);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, String>("Host"));
    }
}
