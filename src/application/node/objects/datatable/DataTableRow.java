package application.node.objects.datatable;

import application.data.DataBank;
import application.node.implementations.DataTableNode;

import java.util.HashMap;

public class DataTableRow {
    private Integer id = -1;
    private HashMap<String, DataTableValue> dataTableValues = new HashMap<>();
    private DataTableNode parentNode;

    public DataTableRow(Integer id, DataTableNode parentNode) {
        this.parentNode = parentNode;
        this.id = id;
        DataBank.loadDataTableValue(this);
    }

    public void updateDataTableValue(String key, String value) {
        if (dataTableValues.containsKey(key)) {
            dataTableValues.get(key).setDataValue(value);
            DataBank.saveDataTableValue(dataTableValues.get(key));
        } else {
            dataTableValues.put(key, DataBank.createNewDataTableValue(this, key, value));
        }
    }

    public void addDataTableValue(DataTableValue dataTableValue) {
        dataTableValues.put(dataTableValue.getDataKey(), dataTableValue);
    }

    public String getData(String column) {
        if (dataTableValues.containsKey(column)) {
            return dataTableValues.get(column).getDataValue();
        } else {
            return "";
        }
    }

    public Integer size() {
        return dataTableValues.size();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DataTableNode getParentNode() {
        return parentNode;
    }

    public HashMap<String, DataTableValue> getDataTableValues() {
        return dataTableValues;
    }
}
