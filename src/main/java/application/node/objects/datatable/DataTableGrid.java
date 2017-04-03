package application.node.objects.datatable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.GridChange;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataTableGrid {
    private ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
    private GridBase grid;
    private ObservableList<DataTableRow> dataTableRows;

    private static Logger log = Logger.getLogger(DataTableGrid.class);

    public DataTableGrid(ObservableList<DataTableRow> dataTableRows) {
        ObservableList<DataTableRow> orderedDataTableRows = FXCollections.observableArrayList();

        // Order columns correctly
        List<DataTableValue> dataTableValues = new ArrayList<>();

        for (DataTableRow dataTableRow : dataTableRows) {
            dataTableValues.addAll(dataTableRow.getDataTableValuesCollection().getOrderedValues());
        }

        Collections.sort(dataTableValues); // Should order by order by in DataTableValue

        for (DataTableValue dataTableValue : dataTableValues) {
            if (!orderedDataTableRows.contains(dataTableValue.getParentRow())) {
                orderedDataTableRows.add(dataTableValue.getParentRow());
            }
        }

        this.dataTableRows = orderedDataTableRows;
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
