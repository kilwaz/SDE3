package sde.application.gui.columns.requesttracker;

import sde.application.net.proxy.MetaRecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class RequestNumberColumn extends TableColumn {
    public RequestNumberColumn() {
        setText("#");
        setPrefWidth(30);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, Integer>("RequestNumber"));
    }
}
