package application.node.objects.datatable;

import application.data.model.DatabaseObject;

import java.util.UUID;

public class DataTableValue extends DatabaseObject {
    private DataTableRow parentRow = null;
    private String dataKey = "";
    private String dataValue = "";

    public DataTableValue() {
       super();
    }

    public String getDataKey() {
        return dataKey;
    }

    public String getDataValue() {
        if (dataValue == null) {
            return "";
        } else {
            return dataValue;
        }
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
        this.save();
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
        this.save();
    }

    public String getParentUuid() {
        if (parentRow != null) {
            return parentRow.getUuidString();
        }
        return null;
    }

    public void setParentRow(DataTableRow parentRow) {
        this.parentRow = parentRow;
    }

    public DataTableRow getParentRow() {
        return parentRow;
    }
}
