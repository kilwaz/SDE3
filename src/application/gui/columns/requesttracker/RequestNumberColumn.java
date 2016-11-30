package application.gui.columns.requesttracker;

import application.net.proxy.MetaRecordedRequest;
import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class RequestNumberColumn extends TableColumn {
    public RequestNumberColumn() {
        setText("#");
        setPrefWidth(30);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, Integer>("RequestNumber"));
    }
}
