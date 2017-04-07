package sde.application.gui.columns.testsetbatchwindow.commandview.cell;

import sde.application.test.TestCommand;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;

public class BooleanTableCell extends TableCell<TestCommand, Boolean> {
    private CheckBox checkBox;

    public BooleanTableCell() {
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
