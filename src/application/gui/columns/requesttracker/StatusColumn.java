package application.gui.columns.requesttracker;

import application.net.proxy.MetaRecordedRequest;
import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class StatusColumn extends TableColumn {
    public StatusColumn() {
        setText("Status");
        setPrefWidth(40);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, Integer>("Status"));
        setCellFactory(column -> new TableCell<RecordedRequest, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null && item > 0) {
                    setText(item.toString());
                } else {
                    setText("");
                }
            }
        });
    }
}
