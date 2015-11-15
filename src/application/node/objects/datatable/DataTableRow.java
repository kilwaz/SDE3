package application.node.objects.datatable;

import application.data.DataBank;
import application.data.model.DatabaseObject;
import application.node.implementations.DataTableNode;

import java.util.LinkedHashMap;
import java.util.UUID;

public class DataTableRow extends DatabaseObject {
    private LinkedHashMap<String, DataTableValue> dataTableValues = new LinkedHashMap<>();
    private DataTableNode parentNode;

    public DataTableRow(UUID uuid, DataTableNode parentNode) {
        super(uuid);
        this.parentNode = parentNode;
        DataBank.loadDataTableValue(this);
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
            dataTableValues.put(key, DataBank.createNewDataTableValue(this, key, value));
        }
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

    public LinkedHashMap<String, DataTableValue> getDataTableValues() {
        return dataTableValues;
    }
}
