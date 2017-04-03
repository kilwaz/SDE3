package sde.application.gui.columns.requesttracker;

import sde.application.net.proxy.MetaRecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class ExtensionColumn extends TableColumn {
    public ExtensionColumn() {
        setText("Extension");
        setPrefWidth(70);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, String>("Extension"));
    }
}
