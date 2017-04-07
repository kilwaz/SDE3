package sde.application.data.export;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;

public class ExportSheet {
    private static Logger log = Logger.getLogger(ExportSheet.class);
    private ExportCell[][] exportValues; // First number is row, second is col, so xy
    private Integer colCount;
    private Integer rowCount;
    private Integer nextFreeRow = 1;
    private String sheetName;

    public ExportSheet() {
        this.colCount = 1;
        this.rowCount = 1;
        this.sheetName = "sheet";

        exportValues = new ExportCell[rowCount][colCount];
    }

    public ExportSheet(Integer rowCount, Integer colCount) {
        this.colCount = colCount;
        this.rowCount = rowCount;

        exportValues = new ExportCell[rowCount][colCount];
    }

    public Integer getNextFreeRow() {
        return nextFreeRow;
    }

    public void add(ExportCell exportValue) {
        if (exportValue.getRowPosition() + 1 > rowCount || exportValue.getColumnPosition() + 1 > colCount) {
            increaseSize(exportValue);
        }
        if (exportValues[exportValue.getRowPosition()] == null) { // Fill in a missing row if it is not there
            exportValues[exportValue.getRowPosition()] = new ExportCell[colCount];
        }
        exportValues[exportValue.getRowPosition()][exportValue.getColumnPosition()] = exportValue;
        if (nextFreeRow <= exportValue.getRowPosition() + 1) {
            nextFreeRow = exportValue.getRowPosition() + 2;
        }
    }

    private void increaseSize(ExportCell exportValue) {
        // Expand rows
        if (exportValue.getRowPosition() + 1 > rowCount) {
            exportValues = Arrays.copyOf(exportValues, exportValue.getRowPosition() + 1);
            rowCount = exportValue.getRowPosition() + 1;
        }

        // Expand columns
        if (exportValue.getColumnPosition() + 1 > colCount) {
            for (int i = 0; i < rowCount; i++) {
                if (exportValues[i] != null) {
                    exportValues[i] = Arrays.copyOf(exportValues[i], exportValue.getColumnPosition() + 1);
                } else {
                    exportValues[i] = new ExportCell[exportValue.getColumnPosition() + 1];
                }
            }
            colCount = exportValue.getColumnPosition() + 1;
        }
    }

    public ExportCell getValue(Integer rowCount, Integer colCount) {
        if (exportValues == null || exportValues[rowCount - 1] == null) {
            return null;
        } else {
            return exportValues[rowCount - 1][colCount - 1];
        }
    }

    public ExportCell[] getRow(Integer rowPos) {
        return exportValues[rowPos];
    }

    public Integer getColCount() {
        return colCount;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void sort(Integer column) {
        sort(column, true, 0);
    }

    public void sort(Integer column, Boolean ascending) {
        sort(column, ascending, 0);
    }

    public void sort(Integer column, Boolean ascending, Integer headerRowCount) {
        ExportCell[][] headers = Arrays.copyOfRange(exportValues, 0, headerRowCount);
        ExportCell[][] data = Arrays.copyOfRange(exportValues, headerRowCount, exportValues.length);

        Arrays.sort(data, (entry1, entry2) -> {
            if ((entry1 == null || entry1[column] == null) && (entry2 == null || entry2[column] != null)) {
                return -1;
            } else if ((entry1 == null || entry1[column] != null) && (entry2 == null || entry2[column] == null)) {
                return 1;
            } else if ((entry1 == null || entry1[column] == null) && (entry2 == null || entry2[column] == null)) {
                return 0;
            }

            ExportCell cell1 = entry1[column];
            ExportCell cell2 = entry2[column];

            if (ascending) {
                return -cell1.compareTo(cell2);
            } else {
                return cell1.compareTo(cell2);
            }
        });

        exportValues = ArrayUtils.addAll(headers, data);
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }
}
