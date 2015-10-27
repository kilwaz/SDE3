package application.node.objects.datatable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.GridChange;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;

public class DataTableGrid {
    private ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
    private GridBase grid;
    private ObservableList<DataTableRow> dataTableRows;

    private static Logger log = Logger.getLogger(DataTableGrid.class);

    public DataTableGrid(ObservableList<DataTableRow> dataTableRows) {
        this.dataTableRows = dataTableRows;
        initGrid();
    }

    public void initGrid() {
        // Default blank grid
        grid = new GridBase(10, 10);
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, this::handleOnChange);
    }

    public void handleOnChange(GridChange change) {
        // Handle method to be overridden
    }

    public ObservableList<DataTableRow> getDataTableRows() {
        return dataTableRows;
    }

    public ObservableList<ObservableList<SpreadsheetCell>> getRows() {
        return rows;
    }

    public GridBase getGrid() {
        return grid;
    }
}
