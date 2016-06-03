package application.gui.columns.requesttracker;

import application.net.proxy.RecordedRequest;
import application.gui.columns.requesttracker.cell.BooleanTableCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class HasParametersColumn extends TableColumn {
    public HasParametersColumn() {
        setText("Params");
        setPrefWidth(50);
        setCellValueFactory(new PropertyValueFactory<RecordedRequest, Boolean>("HasParameters"));

        Callback<TableColumn<RecordedRequest, Boolean>, TableCell<RecordedRequest, Boolean>> booleanCellFactory = p -> new BooleanTableCell();
        setCellFactory(booleanCellFactory);
    }
}

