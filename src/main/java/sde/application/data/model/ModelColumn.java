package sde.application.data.model;

import java.lang.reflect.Method;

public class ModelColumn {
    public static final Integer STANDARD_COLUMN = 1;
    public static final Integer BLOB_COLUMN = 2;

    private String columnName;
    private Method objectSaveMethod;
    private Method objectLoadMethod;
    private Integer type;

    public ModelColumn(String columnName, Method objectSaveMethod, Method objectLoadMethod, Integer type) {
        this.columnName = columnName;
        this.objectSaveMethod = objectSaveMethod;
        this.objectLoadMethod = objectLoadMethod;
        this.type = type;
    }

    public String getColumnName() {
        return columnName;
    }

    public Method getObjectSaveMethod() {
        return objectSaveMethod;
    }

    public Method getObjectLoadMethod() {
        return objectLoadMethod;
    }

    public Integer getType() {
        return type;
    }
}
