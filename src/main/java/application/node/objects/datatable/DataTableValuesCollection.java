package application.node.objects.datatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataTableValuesCollection {
    private List<DataTableValue> dataTableValueList = new ArrayList<>();
    private List<String> keysContained = new ArrayList<>();

    public DataTableValuesCollection() {

    }

    public void put(DataTableValue dataTableValue) {
        // If a value already exists in the collection with the same key, we override it.
        if (keysContained.contains(dataTableValue.getDataKey())) {
            remove(dataTableValue);
        }
        dataTableValueList.add(dataTableValue);
        keysContained.add(dataTableValue.getDataKey());
        sort();
    }

    public Boolean containsKey(String key) {
        return keysContained.contains(key);
    }

    public void remove(DataTableValue dataTableValue) {
        keysContained.remove(dataTableValue.getDataKey());
        dataTableValueList.remove(dataTableValue);
    }

    public void remove(String key) {
        if (key != null) {
            DataTableValue dataTableValueToRemove = get(key);
            if (dataTableValueToRemove != null) {
                remove(dataTableValueToRemove);
            }
        }
    }

    public DataTableValue get(String key) {
        if (key != null) {
            for (DataTableValue dataTableValue : dataTableValueList) {
                if (key.equals(dataTableValue.getDataKey())) {
                    return dataTableValue;
                }
            }
        }
        return null;
    }

    public List<String> getKeys() {
        return keysContained;
    }

    public Integer size() {
        return dataTableValueList.size();
    }

    public List<DataTableValue> getOrderedValues() {
        return dataTableValueList;
    }

    public void sort() {
        Collections.sort(dataTableValueList);
    }
}
