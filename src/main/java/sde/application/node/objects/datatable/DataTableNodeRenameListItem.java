package sde.application.node.objects.datatable;

public class DataTableNodeRenameListItem {
    private String value;
    private Integer order;

    public DataTableNodeRenameListItem(String value, Integer order) {
        this.value = value;
        this.order = order;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String toString() {
        return order + ": " + value;
    }

    public boolean equals(String str) {
        if (value != null && str != null) {
            if (value.equals(str)) {
                return true;
            }
        }

        return false;
    }
}
