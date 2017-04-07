package sde.application.node.objects.datatable;

import sde.application.data.model.DatabaseObject;
import sde.application.node.implementations.DataTableNode;

import java.util.List;

public class DataTableRow extends DatabaseObject {
    private DataTableValuesCollection dataTableValuesCollection = new DataTableValuesCollection();
    private DataTableNode parentNode;

    public DataTableRow() {
        super();
    }

    public void removeDataTableValue(String key) {
        if (dataTableValuesCollection.containsKey(key)) {
            dataTableValuesCollection.get(key).delete();
            dataTableValuesCollection.remove(key);
        }
    }

    public void updateDataTableValue(String key, String value) {
        if (dataTableValuesCollection.containsKey(key)) {
            dataTableValuesCollection.get(key).setDataValue(value);
            dataTableValuesCollection.get(key).save();
        } else {
            DataTableValue dataTableValue = DataTableValue.create(DataTableValue.class);
            dataTableValue.setParentRow(this);
            dataTableValue.setDataKey(key);
            dataTableValue.setDataValue(value);
            dataTableValue.save();
            dataTableValuesCollection.put(dataTableValue);
        }
    }

    public void addAllDataTableValue(List<DataTableValue> dataTableValueList) {
        dataTableValueList.forEach(this::addDataTableValue);
    }

    public void addDataTableValue(DataTableValue dataTableValue) {
        dataTableValuesCollection.put(dataTableValue);
    }

    public String getData(String key) {
        if (dataTableValuesCollection.containsKey(key)) {
            return dataTableValuesCollection.get(key).getDataValue();
        } else {
            return "";
        }
    }

    public Integer size() {
        return dataTableValuesCollection.size();
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

    public DataTableValuesCollection getDataTableValuesCollection() {
        return dataTableValuesCollection;
    }
}
