package application.gui.columns.requesttracker;

import application.gui.columns.requesttracker.cell.BooleanTableCell;
import application.net.proxy.MetaRecordedRequest;
import application.net.proxy.RecordedRequest;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class SSLColumn extends TableColumn {
    public SSLColumn() {
        setText("SSL");
        setPrefWidth(40);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, Boolean>("Https"));

        Callback<TableColumn<RecordedRequest, Boolean>, TableCell<MetaRecordedRequest, Boolean>> booleanCellFactory = p -> new BooleanTableCell();
        setCellFactory(booleanCellFactory);
    }
}

