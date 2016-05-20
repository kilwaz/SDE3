package application.node.objects.datatable;

import application.data.model.DatabaseObject;
import org.apache.log4j.Logger;

public class DataTableValue extends DatabaseObject implements Comparable<DataTableValue> {
    private static Logger log = Logger.getLogger(DataTableValue.class);
    private DataTableRow parentRow = null;
    private String dataKey = "";
    private String dataValue = "";
    private Integer order = 0;

    public DataTableValue() {
        super();
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public String getDataValue() {
        if (dataValue == null) {
            return "";
        } else {
            return dataValue;
        }
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
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

    public void setParentRow(DataTableRow parentRow) {
        this.parentRow = parentRow;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public DataTableNodeRenameListItem getAsListItem() {
        return new DataTableNodeRenameListItem(getDataKey(), getOrder());
    }

    public int compareTo(DataTableValue dataTableValue) {
        // Ascending order
        return this.order - dataTableValue.getOrder();
    }
}
