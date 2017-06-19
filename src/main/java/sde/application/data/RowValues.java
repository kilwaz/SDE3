package sde.application.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RowValues {
    List<RowValue> rowValues = new ArrayList<>();
    HashMap<String, Integer> columnMap = new HashMap<>();

    public RowValues() {

    }

    public void addValue(RowValue rowValue) {
        columnMap.put(rowValue.getName(), rowValues.size());
        rowValues.add(rowValue);
    }

    public RowValue get(String columnName) {
        Integer index = columnMap.get(columnName);
        if (index != -1) {
            return rowValues.get(index);
        }

        return null;
    }

    public RowValue get(Integer index) {
        return rowValues.get(index);
    }
}
