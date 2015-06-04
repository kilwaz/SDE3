package application.data.export;

public class ExportValue {
    private Integer rowPosition;
    private Integer columnPosition;
    private Object dataValue;

    public ExportValue(Object dataValue, Integer rowPosition, Integer columnPosition) {
        this.dataValue = dataValue;
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

    public Object getDataValue() {
        return dataValue;
    }

    public void setDataValue(Object dataValue) {
        this.dataValue = dataValue;
    }
}
