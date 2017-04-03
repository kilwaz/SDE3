package sde.application.gui.columns.requesttracker;

import sde.application.net.proxy.MetaRecordedRequest;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class TitleColumn extends TableColumn {
    public TitleColumn() {
        setText("Title");
        setPrefWidth(80);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, String>("Title"));
    }
}
