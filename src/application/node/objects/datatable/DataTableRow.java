package application.node.objects.datatable;

import application.data.model.DatabaseObject;
import application.node.implementations.DataTableNode;

import java.util.LinkedHashMap;
import java.util.List;

public class DataTableRow extends DatabaseObject {
    private LinkedHashMap<String, DataTableValue> dataTableValues = new LinkedHashMap<>();
    private DataTableNode parentNode;

    public DataTableRow() {
        super();
    }

    public void removeDataTableValue(String key) {
        if (dataTableValues.containsKey(key)) {
            dataTableValues.get(key).delete();
            dataTableValues.remove(key);
        }
    }

    public void updateDataTableValue(String key, String value) {
        if (dataTableValues.containsKey(key)) {
            dataTableValues.get(key).setDataValue(value);
            dataTableValues.get(key).save();
        } else {
            DataTableValue dataTableValue = DataTableValue.create(DataTableValue.class);
            dataTableValue.setParentRow(this);
            dataTableValue.setDataKey(key);
            dataTableValue.setDataValue(value);
            dataTableValue.save();
            dataTableValues.put(key, dataTableValue);
        }
    }

    public void addAllDataTableValue(List<DataTableValue> dataTableValueList) {
        dataTableValueList.forEach(this::addDataTableValue);
    }

    public void addDataTableValue(DataTableValue dataTableValue) {
        dataTableValues.put(dataTableValue.getDataKey(), dataTableValue);
    }

    public String getData(String key) {
        if (dataTableValues.containsKey(key)) {
            return dataTableValues.get(key).getDataValue();
        } else {
            return "";
        }
    }

    public Integer size() {
        return dataTableValues.size();
    }

    public DataTableNode getParentNode() {
        return parentNode;
    }

    public String getParentUuid() {
        if (parentNode != null) {
            return parentNode.getUuidString();
        }
        return null;
    }

    public void setParent(DataTableNode parentNode) {
        this.parentNode = parentNode;
    }

    public LinkedHashMap<String, DataTableValue> getDataTableValues() {
        return dataTableValues;
    }
}
