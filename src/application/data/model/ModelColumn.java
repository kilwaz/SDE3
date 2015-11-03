package application.data.model;

import java.lang.reflect.Method;

public class ModelColumn {
    private String columnName;
    private Method objectMethod;

    public ModelColumn(String columnName, Method objectMethod) {
        this.columnName = columnName;
        this.objectMethod = objectMethod;
    }

    public String getColumnName() {
        return columnName;
    }

    public Method getObjectMethod() {
        return objectMethod;
    }
}
