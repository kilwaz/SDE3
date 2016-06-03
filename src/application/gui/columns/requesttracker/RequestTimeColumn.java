package application.gui.columns.requesttracker;

import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class RequestTimeColumn extends TableColumn {
    private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("kk:mm:ss dd MMM yyyy");

    public RequestTimeColumn() {
        setText("Time");
        setPrefWidth(140);
        setCellValueFactory(new PropertyValueFactory<RecordedRequest, DateTime>("ResponseDateTimeFromHeaders"));
        setCellFactory(column -> new TableCell<RecordedRequest, DateTime>() {
            @Override
            protected void updateItem(DateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    String formattedItemStr = dateFormatter.print(item);
                    setText(formattedItemStr);
                } else {
                    setText("");
                }
            }
        });
    }
}
