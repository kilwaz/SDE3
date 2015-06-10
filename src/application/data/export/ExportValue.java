package application.data.export;

public class ExportValue extends ExportCell {
    private Object dataValue;

    public ExportValue(Object dataValue, Integer rowPosition, Integer columnPosition) {
        super(rowPosition, columnPosition);
        this.dataValue = dataValue;
    }

    public Object getDataValue() {
        return dataValue;
    }

    public void setDataValue(Object dataValue) {
        this.dataValue = dataValue;
    }

}
