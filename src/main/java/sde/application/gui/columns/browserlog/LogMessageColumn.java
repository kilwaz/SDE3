package sde.application.gui.columns.browserlog;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import sde.application.node.objects.BrowserLog;

public class LogMessageColumn extends TableColumn {
    public LogMessageColumn() {
        setText("Message");
        setPrefWidth(360);
        setCellValueFactory(new PropertyValueFactory<BrowserLog, String>("LogMessage"));
    }
}
