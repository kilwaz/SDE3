package sde.application.data.model;

public class ModelChild {
    private String referenceColumn;
    private Class databaseLinkClass;

    public ModelChild(Class databaseLinkClass, String referenceColumn) {
        this.referenceColumn = referenceColumn;
        this.databaseLinkClass = databaseLinkClass;
    }

    public String getReferenceColumn() {
        return referenceColumn;
    }

    public void setReferenceColumn(String referenceColumn) {
        this.referenceColumn = referenceColumn;
    }

    public DatabaseLink getDatabaseLink() {
        return DatabaseLink.getNewInstance(databaseLinkClass);
    }

    public Class getDatabaseLinkClass() {
        return databaseLinkClass;
    }

    public void setDatabaseLinkClass(Class databaseLinkClass) {
        this.databaseLinkClass = databaseLinkClass;
    }
}
