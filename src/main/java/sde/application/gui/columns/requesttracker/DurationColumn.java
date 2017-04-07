package sde.application.gui.columns.requesttracker;

import sde.application.net.proxy.MetaRecordedRequest;
import sde.application.net.proxy.RecordedRequest;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.DecimalFormat;

public class DurationColumn extends TableColumn {
    private static DecimalFormat numberFormatter = new DecimalFormat("###,###");

    public DurationColumn() {
        setText("Duration");
        setPrefWidth(60);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, Integer>("Duration"));
        setCellFactory(column -> new TableCell<RecordedRequest, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null && item > 0) {
                    String formattedItemStr = numberFormatter.format(item);
                    setText(formattedItemStr + "ms");
                } else {
                    setText("Processing...");
                }
            }
        });
    }
}
