package application.data.export;

import org.apache.log4j.Logger;

public class ExportCell {
    private Integer rowPosition;
    private Integer columnPosition;

    private static Logger log = Logger.getLogger(ExportCell.class);

    public ExportCell(Integer rowPosition, Integer columnPosition) {
        this.rowPosition = rowPosition;
        this.columnPosition = columnPosition;
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

    int compareTo(Object cell){
        return 0;
    }
}
