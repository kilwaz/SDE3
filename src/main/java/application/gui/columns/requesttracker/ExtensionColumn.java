package application.gui.columns.requesttracker;

import application.net.proxy.MetaRecordedRequest;
import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class ExtensionColumn extends TableColumn {
    public ExtensionColumn() {
        setText("Extension");
        setPrefWidth(70);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, String>("Extension"));
    }
}
