package application.node.objects.trackercolumn;

import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class CookieColumn extends TableColumn {
    public CookieColumn() {
        setText("Cookies");
        setPrefWidth(70);
        setCellValueFactory(new PropertyValueFactory<RecordedRequest, String>("Cookies"));
    }
}
