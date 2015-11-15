package application.node.objects.datatable;

import application.data.model.DatabaseObject;

import java.util.UUID;

public class DataTableValue extends DatabaseObject {
    private DataTableRow parentRow = null;
    private String dataKey = "";
    private String dataValue = "";

    public DataTableValue(UUID uuid, String dataKey, String dataValue, DataTableRow parentRow) {
        super(uuid);
        this.parentRow = parentRow;
        this.dataKey = dataKey;
        this.dataValue = dataValue;
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

    public DataTableRow getParentRow() {
        return parentRow;
    }
}
