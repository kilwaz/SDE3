package application.data.export;

import org.apache.log4j.Logger;

public class ExportCell {
    private Integer rowPosition = null;
    private Integer columnPosition = null;
    private String cellColour = null;

    private static Logger log = Logger.getLogger(ExportCell.class);

    public ExportCell(Integer rowPosition, Integer columnPosition) {
        this.rowPosition = rowPosition;
        this.columnPosition = columnPosition;
    }

    public ExportCell(Integer rowPosition, Integer columnPosition, String cellColour) {
        this.rowPosition = rowPosition;
        this.columnPosition = columnPosition;
        this.cellColour = cellColour;
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
