package sde.application.gui.columns.inspect;

import sde.application.net.proxy.RecordedHeader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class HeaderColumn extends TableColumn {
    public HeaderColumn() {
        setText("Value");
        setPrefWidth(70);
        setCellValueFactory(new PropertyValueFactory<RecordedHeader, String>("Value"));
    }
}
