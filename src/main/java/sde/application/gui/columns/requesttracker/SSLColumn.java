package sde.application.gui.columns.requesttracker;

import sde.application.gui.columns.requesttracker.cell.BooleanTableCell;
import sde.application.net.proxy.MetaRecordedRequest;
import sde.application.net.proxy.RecordedRequest;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class SSLColumn extends TableColumn {
    public SSLColumn() {
        setText("KeyStore");
        setPrefWidth(40);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, Boolean>("Https"));

        Callback<TableColumn<RecordedRequest, Boolean>, TableCell<MetaRecordedRequest, Boolean>> booleanCellFactory = p -> new BooleanTableCell();
        setCellFactory(booleanCellFactory);
    }
}

