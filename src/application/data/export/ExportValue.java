package application.data.export;

import org.apache.log4j.Logger;

public class ExportValue extends ExportCell {
    private static Logger log = Logger.getLogger(ExportValue.class);
    private Object dataValue;

    public ExportValue(Object dataValue, Integer rowPosition, Integer columnPosition) {
        super(rowPosition, columnPosition);
        this.dataValue = dataValue;
    }

    public ExportValue(Object dataValue, Integer rowPosition, Integer columnPosition, String cellColour) {
        super(rowPosition, columnPosition, cellColour);
        this.dataValue = dataValue;
    }

    public Object getDataValue() {
        return dataValue;
    }

    public void setDataValue(Object dataValue) {
        this.dataValue = dataValue;
    }

    int compareTo(Object cell) {
        if (cell != null && cell instanceof ExportValue) {
            Object var1 = ((ExportValue) cell).getDataValue();
            Object var2 = dataValue;

            if (var1 instanceof String && var2 instanceof String) {
                return ((String) var1).compareTo(var2.toString());
            } else if (var1 instanceof Integer && var2 instanceof Integer) {
                return ((Integer) var1).compareTo((Integer) var2);
            } else if (var1 instanceof Double && var2 instanceof Double) {
                return ((Double) var1).compareTo((Double) var2);
            } else {
                return (var1.toString()).compareTo(var2.toString());
            }
        }

        return 0;
    }
}
