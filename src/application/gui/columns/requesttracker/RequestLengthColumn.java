package application.gui.columns.requesttracker;

import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.DecimalFormat;

public class RequestLengthColumn extends TableColumn {
    private static DecimalFormat numberFormatter = new DecimalFormat("###,###");

    public RequestLengthColumn() {
        setText("Request Length");
        setPrefWidth(100);
        setCellValueFactory(new PropertyValueFactory<RecordedRequest, Integer>("RequestSize"));
        setCellFactory(column -> new TableCell<RecordedRequest, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    String formattedItemStr = numberFormatter.format(item);
                    setText(formattedItemStr);
                } else {
                    setText("Processing...");
                }
            }
        });
    }
}
