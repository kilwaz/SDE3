package sde.application.gui.columns.requesttracker;

import sde.application.net.proxy.MetaRecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class URLColumn extends TableColumn {
    public URLColumn() {
        setText("URL");
        setPrefWidth(200);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, String>("Url"));
    }
}
