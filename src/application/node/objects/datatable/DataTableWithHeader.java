package application.node.objects.datatable;

import application.node.implementations.DataTableNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.GridChange;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;

import java.util.LinkedHashMap;
import java.util.List;

public class DataTableWithHeader extends DataTableGrid {
    private GridBase grid;
    private static Logger log = Logger.getLogger(DataTableWithHeader.class);

    private LinkedHashMap<String, DataTableValue> dataTableValuePositionHashMap;

    public DataTableWithHeader(ObservableList<DataTableRow> dataTableRows) {
        super(dataTableRows);
    }

    public void handleOnChange(GridChange change) {
        DataTableValue dataTableValue = dataTableValuePositionHashMap.get(change.getRow() + "," + change.getColumn());
        if (dataTableValue != null) {
            dataTableValue.setDataValue((String) change.getNewValue());
            dataTableValue.save();
        }
    }

    public void initGrid() {
        dataTableValuePositionHashMap = new LinkedHashMap<>();

        Integer rowCount = 0;
        Integer colCount = 0;

        List<DataTableNodeRenameListItem> columnNames = DataTableNode.findColumnNames(getDataTableRows());

        for (DataTableRow dataTableRow : getDataTableRows()) {
            ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();

            DataTableValuesCollection dataTableValues = dataTableRow.getDataTableValuesCollection();

            if (dataTableValues.size() > 0) {
                colCount = 0;

                for (DataTableValue dataTableValue : dataTableValues.getOrderedValues()) {
                    SpreadsheetCell spreadsheetCell = SpreadsheetCellType.STRING.createCell(rowCount, colCount, 1, 1, dataTableValue.getDataValue());

                    dataTableValuePositionHashMap.put(rowCount + "," + colCount, dataTableValue);

                    list.add(spreadsheetCell);
                    colCount++;
                }

                for (int i = colCount; i < columnNames.size(); i++) {
                    SpreadsheetCell spreadsheetCell = SpreadsheetCellType.STRING.createCell(rowCount, colCount, 1, 1, "Extra");
                    list.add(spreadsheetCell);
                    colCount++;
                }
            } else {
                for (int i = 0; i < columnNames.size(); i++) {
                    DataTableValue dataTableValue = DataTableValue.create(DataTableValue.class);
                    dataTableValue.setParentRow(dataTableRow);
                    dataTableValue.setDataKey(columnNames.get(i).getValue());
                    dataTableValue.setDataValue("");
                    dataTableValue.save();
                    dataTableRow.addDataTableValue(dataTableValue);
                    SpreadsheetCell spreadsheetCell = SpreadsheetCellType.STRING.createCell(rowCount, i, 1, 1, dataTableValue.getDataValue());
                    dataTableValuePositionHashMap.put(rowCount + "," + i, dataTableValue);
                    list.add(spreadsheetCell);
                }
            }
            getRows().add(list);
            rowCount++;
        }

        grid = new GridBase(getDataTableRows().size(), columnNames.size());
        for (DataTableNodeRenameListItem dataTableNodeRenameListItem : columnNames) {
            grid.getColumnHeaders().add(dataTableNodeRenameListItem.getValue());
        }

        grid.setRows(getRows());
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, this::handleOnChange);
    }

    public void deleteRow(Integer rowNumber) {
        getDataTableRows().remove(rowNumber);
    }

    public GridBase getGrid() {
        return grid;
    }
}
