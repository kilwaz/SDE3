package application.gui.columns.testsetbatchwindow.statecompare;


import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;

import static application.error.Error.STATE_COMPARE_ELEMENT_CELL_ERROR;

public class StateCompareElementCell extends TableCell<CompareStateElementObject, Object> {
    public StateCompareElementCell() {

    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        try {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                if (item instanceof String) {
                    setText((String) item);
                } else if (item instanceof Double) {
                    setText((item).toString());
                }
            } else {
                setText(null);
            }

            TableRow<CompareStateElementObject> tableRow = getTableRow();
            if (tableRow.getItem() == null) {
                setStyle("-fx-background-color:blanchedalmond ");
            } else if (tableRow.getItem().getMatched()) {
                setStyle("-fx-background-color:lightgreen");
            } else {
                setStyle("-fx-background-color:salmon");
            }
        } catch (Exception ex) {
            STATE_COMPARE_ELEMENT_CELL_ERROR.record().create(ex);
        }
    }
}
