package application.node.objects.trackercolumn.cell;

import application.net.proxy.RecordedRequest;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;

public class BooleanCell extends TableCell<RecordedRequest, Boolean> {
    private CheckBox checkBox;

    public BooleanCell() {
        checkBox = new CheckBox();
        this.setGraphic(checkBox);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.setEditable(false);
    }

    @Override
    public void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (!isEmpty()) {
            checkBox.setSelected(item);
        }
    }
}
