package application.node.objects.trackercolumn;

import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class RedirectColumn extends TableColumn {
    public RedirectColumn() {
        setText("Redirect");
        setPrefWidth(200);
        setCellValueFactory(new PropertyValueFactory<RecordedRequest, String>("RedirectHost"));
    }
}
