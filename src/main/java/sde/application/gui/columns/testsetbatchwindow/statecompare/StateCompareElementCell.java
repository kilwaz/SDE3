package sde.application.gui.columns.testsetbatchwindow.statecompare;


import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import org.apache.log4j.Logger;
import sde.application.error.Error;

public class StateCompareElementCell extends TableCell<CompareStateElementObject, Object> {
    private static Logger log = Logger.getLogger(StateCompareElementCell.class);

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
            if (tableRow != null) {
                if (tableRow.getItem() == null) { // Empty row
                    setStyle("");
                } else if (tableRow.getItem().getMatched()) { // Change that we checked for an happened
                    setStyle("-fx-background-color:lightgreen");
                } else if (tableRow.getItem().isUnexpectedChange()) { // Change that we didn't check for and happened
                    setStyle("-fx-background-color:salmon");
                } else if (!tableRow.getItem().getMatched()) { // Change that we checked for an didn't happen
                    setStyle("-fx-background-color:blanchedalmond");
                }
            }
        } catch (Exception ex) {
            Error.STATE_COMPARE_ELEMENT_CELL_ERROR.record().create(ex);
        }
    }
}
