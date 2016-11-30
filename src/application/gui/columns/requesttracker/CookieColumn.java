package application.gui.columns.requesttracker;

import application.net.proxy.MetaRecordedRequest;
import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class CookieColumn extends TableColumn {
    public CookieColumn() {
        setText("Cookies");
        setPrefWidth(70);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, String>("Cookies"));
    }
}
