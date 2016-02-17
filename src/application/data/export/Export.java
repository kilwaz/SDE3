package application.data.export;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;

public class Export {
    private static Logger log = Logger.getLogger(Export.class);
    private ExportCell[][] exportValues; // First number is row, second is col, so xy
    private Integer colCount;
    private Integer rowCount;
    private Integer nextFreeRow = 0;

    public Export(Integer rowCount, Integer colCount) {
        this.colCount = colCount;
        this.rowCount = rowCount;

        exportValues = new ExportCell[rowCount][colCount];
    }

    public Integer getNextFreeRow() {
        return nextFreeRow;
    }

    public void add(ExportCell exportValue) {
        exportValues[exportValue.getRowPosition()][exportValue.getColumnPosition()] = exportValue;
        if (nextFreeRow <= exportValue.getRowPosition()) {
            nextFreeRow = exportValue.getRowPosition() + 1;
        }
    }

    public ExportCell getValue(Integer rowCount, Integer colCount) {
        return exportValues[rowCount][colCount];
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
            final ExportCell time1 = entry1[column];
            final ExportCell time2 = entry2[column];

            if (time1 != null) {
                if (ascending) {
                    return -time1.compareTo(time2);
                } else {
                    return time1.compareTo(time2);
                }
            } else {
                return 0;
            }
        });

        exportValues = ArrayUtils.addAll(headers, data);
    }
}
