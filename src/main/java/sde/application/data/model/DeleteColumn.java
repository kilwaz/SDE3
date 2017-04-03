package sde.application.data.model;

public class DeleteColumn {
    private String columnName;
    private Class<DatabaseObject> classToDelete;

    public DeleteColumn(String columnName, Class<DatabaseObject> classToDelete) {
        this.columnName = columnName;
        this.classToDelete = classToDelete;
    }

    public String getColumnName() {
        return columnName;
    }

    public Class<DatabaseObject> getClassToDelete() {
        return classToDelete;
    }
}
