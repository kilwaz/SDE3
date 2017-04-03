package sde.application.gui.columns.inspect;

import sde.application.net.proxy.RecordedHeader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import static javafx.scene.control.cell.TextFieldTableCell.forTableColumn;

public class NameColumn extends TableColumn {
    public NameColumn() {
        setText("Name");
        setPrefWidth(70);
        setCellValueFactory(new PropertyValueFactory<RecordedHeader, String>("Name"));

        TextFieldTableCell<RecordedHeader, String> cell = new TextFieldTableCell<>();
        setCellFactory(cell.forTableColumn());
        setEditable(true);
    }
}
