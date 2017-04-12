package sde.application.gui.columns.browserlog;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import sde.application.net.proxy.MetaRecordedRequest;
import sde.application.node.objects.BrowserLog;

public class LogLevelColumn extends TableColumn {
    public LogLevelColumn() {
        setText("Level");
        setPrefWidth(100);
        setCellValueFactory(new PropertyValueFactory<BrowserLog, String>("LogLevel"));
    }
}
