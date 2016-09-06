package application.data.export;

import application.error.Error;
import org.apache.log4j.Logger;

public class ExportCell {
    private static Logger log = Logger.getLogger(ExportCell.class);
    private Integer rowPosition = null;
    private Integer columnPosition = null;
    private String cellColour = null;

    public ExportCell(Integer rowPosition, Integer columnPosition) {
        setupPositionValues(rowPosition, columnPosition);
    }

    public ExportCell(Integer rowPosition, Integer columnPosition, String cellColour) {
        setupPositionValues(rowPosition, columnPosition);
        this.cellColour = cellColour;
    }

    private void setupPositionValues(Integer rowPosition, Integer columnPosition) {
        this.rowPosition = rowPosition - 1; // Arrays start at 0 but user see 1
        this.columnPosition = columnPosition - 1; // Arrays start at 0 but user see 1

        if (this.rowPosition < 0 || this.columnPosition < 0) {
            Error.EXPORT_NEGATIVE_VALUES.record().additionalInformation(rowPosition + "," + columnPosition).create();
        }
    }

    public Integer getRowPosition() {
        return rowPosition;
    }

    public void setRowPosition(Integer rowPosition) {
        this.rowPosition = rowPosition;
    }

    public Integer getColumnPosition() {
        return columnPosition;
    }

    public void setColumnPosition(Integer columnPosition) {
        this.columnPosition = columnPosition;
    }

    public String getCellColour() {
        return cellColour;
    }

    public void setCellColour(String cellColour) {
        this.cellColour = cellColour;
    }

    int compareTo(Object cell) {
        return 0;
    }
}
