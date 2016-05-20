package application.node.objects.trackercolumn;

import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class TitleColumn extends TableColumn {
    public TitleColumn() {
        setText("Title");
        setPrefWidth(80);
        setCellValueFactory(new PropertyValueFactory<RecordedRequest, String>("Title"));
    }
}
