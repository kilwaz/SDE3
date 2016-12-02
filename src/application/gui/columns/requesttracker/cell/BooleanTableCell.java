package application.gui.columns.requesttracker.cell;

import application.net.proxy.MetaRecordedRequest;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;

public class BooleanTableCell extends TableCell<MetaRecordedRequest, Boolean> {
    private CheckBox checkBox;

    public BooleanTableCell() {
        checkBox = new CheckBox();
        checkBox.setDisable(true);
        checkBox.setStyle("-fx-opacity: 1"); // Make it look normal but while being disabled
        this.setGraphic(checkBox);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    public void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (!isEmpty()) {
            checkBox.setSelected(item);
        }
    }
}
