package application.gui.columns.requesttracker;

import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class MediaTypeColumn extends TableColumn {
    public MediaTypeColumn() {
        setText("MIME type");
        setPrefWidth(80);
        setCellValueFactory(new PropertyValueFactory<RecordedRequest, String>("MediaType"));
    }
}
