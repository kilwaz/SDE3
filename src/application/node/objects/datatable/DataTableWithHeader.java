package application.node.objects.datatable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.GridChange;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DataTableWithHeader extends DataTableGrid {
    private GridBase grid;
    private static Logger log = Logger.getLogger(DataTableWithHeader.class);

    private HashMap<String, DataTableValue> dataTableValuePositionHashMap;

    public DataTableWithHeader(ObservableList<DataTableRow> dataTableRows) {
        super(dataTableRows);
    }

    public void handleOnChange(GridChange change) {
        //log.info("EVENT: " + change.getRow() + "," + change.getColumn() + " OLD " + change.getOldValue() + " -> " + change.getNewValue());

        DataTableValue dataTableValue = dataTableValuePositionHashMap.get(change.getRow() + "," + change.getColumn());
        if (dataTableValue != null) {
            dataTableValue.setDataValue((String) change.getNewValue());
            //log.info("Saved!");
        }
    }

    public void initGrid() {
        dataTableValuePositionHashMap = new HashMap<>();

        Integer rowCount = 0;
        Integer colCount = 0;

        grid = new GridBase(15, 10);

        List<String> columnNames = new ArrayList<>();

        for (DataTableRow dataTableRow : getDataTableRows()) {
            ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();

            LinkedHashMap<String, DataTableValue> rowValues = dataTableRow.getDataTableValues();

            if (rowValues.size() > 0) {
                colCount = 0;

                for (DataTableValue dataTableValue : rowValues.values()) {
                    if (!columnNames.contains(dataTableValue.getDataKey())) {
                        columnNames.add(dataTableValue.getDataKey());
                    }

                    SpreadsheetCell spreadsheetCell = SpreadsheetCellType.STRING.createCell(rowCount, colCount, 1, 1, dataTableValue.getDataValue());

                    dataTableValuePositionHashMap.put(rowCount + "," + colCount, dataTableValue);

                    list.add(spreadsheetCell);
                    colCount++;
                }
                getRows().add(list);

                rowCount++;
            }
        }

        grid.getColumnHeaders().addAll(columnNames);

        grid.setRows(getRows());
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, this::handleOnChange);
    }

    public GridBase getGrid() {
        return grid;
    }
}
