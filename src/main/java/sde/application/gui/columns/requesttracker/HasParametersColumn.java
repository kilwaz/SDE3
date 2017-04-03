package sde.application.gui.columns.requesttracker;

import sde.application.gui.columns.requesttracker.cell.BooleanTableCell;
import sde.application.net.proxy.MetaRecordedRequest;
import sde.application.net.proxy.RecordedRequest;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class HasParametersColumn extends TableColumn {
    public HasParametersColumn() {
        setText("Params");
        setPrefWidth(50);
        setCellValueFactory(new PropertyValueFactory<MetaRecordedRequest, Boolean>("HasParameters"));

        Callback<TableColumn<RecordedRequest, Boolean>, TableCell<MetaRecordedRequest, Boolean>> booleanCellFactory = p -> new BooleanTableCell();
        setCellFactory(booleanCellFactory);
    }
}

