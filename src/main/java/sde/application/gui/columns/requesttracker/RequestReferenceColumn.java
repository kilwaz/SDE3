package sde.application.gui.columns.requesttracker;


import sde.application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class RequestReferenceColumn extends TableColumn {
    public RequestReferenceColumn() {
        setText("Ref");
        setPrefWidth(50);
        setCellValueFactory(new PropertyValueFactory<RecordedRequest, String>("Reference"));
    }
}

