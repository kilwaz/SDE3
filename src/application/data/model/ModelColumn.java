package application.data.model;

import java.lang.reflect.Method;

public class ModelColumn {
    public static final Integer STANDARD_COLUMN = 1;
    public static final Integer BLOB_COLUMN = 2;

    private String columnName;
    private Method objectMethod;
    private Integer type;

    public ModelColumn(String columnName, Method objectMethod, Integer type) {
        this.columnName = columnName;
        this.objectMethod = objectMethod;
        this.type = type;
    }

    public String getColumnName() {
        return columnName;
    }

    public Method getObjectMethod() {
        return objectMethod;
    }

    public Integer getType() {
        return type;
    }
}
