package sde.application.gui.columns.browserlog;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import sde.application.net.proxy.RecordedRequest;
import sde.application.node.objects.BrowserLog;

public class LogDateColumn extends TableColumn {
    private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("kk:mm:ss dd MMM yyyy");

    public LogDateColumn() {
        setText("Date");
        setPrefWidth(140);
        setCellValueFactory(new PropertyValueFactory<BrowserLog, DateTime>("LogDateTime"));
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
