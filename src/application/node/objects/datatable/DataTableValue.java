package application.node.objects.datatable;

import application.data.DataBank;

public class DataTableValue {
    private Integer id = -1;
    private DataTableRow parentRow = null;
    private String dataKey = "";
    private String dataValue = "";

    public DataTableValue(Integer id, String dataKey, String dataValue, DataTableRow parentRow) {
        this.parentRow = parentRow;
        this.dataKey = dataKey;
        this.dataValue = dataValue;
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDataKey() {
        return dataKey;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
        DataBank.saveDataTableValue(this);
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
        DataBank.saveDataTableValue(this);
    }

    public DataTableRow getParentRow() {
        return parentRow;
    }
}
