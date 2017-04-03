package application.gui.columns.requesttracker;

import application.net.proxy.MetaRecordedRequest;
import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class MethodColumn extends TableColumn {
    public MethodColumn() {
        setText("Method");
        setPrefWidth(50);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, String>("Method"));
    }
}
