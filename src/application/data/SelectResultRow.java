package application.data;

import java.math.BigInteger;
import java.util.HashMap;

public class SelectResultRow {
    HashMap<String, Object> rowValues = new HashMap<>();

    public SelectResultRow() {
    }

    public void addColumn(String colName, Object colValue) {
        rowValues.put(colName, colValue);
    }

    public Object getColumnObject(String colName) {
        return rowValues.get(colName);
    }

    public Integer getInt(String colName) {
        return (Integer) rowValues.get(colName);
    }

    public String getString(String colName) {
        return (String) rowValues.get(colName);
    }

    public Double getDouble(String colName) {
        return (Double) rowValues.get(colName);
    }

    public Boolean getBoolean(String colName) {
        return (Boolean) rowValues.get(colName);
    }

    public BigInteger getBigInt(String colName) {
        return (BigInteger) rowValues.get(colName);
    }

    public String getBlobString(String colName) {
        return (String) rowValues.get(colName);
    }

    public Integer getBlobInt(String colName) {
        return Integer.parseInt((String) rowValues.get(colName));
    }

    public Double getBlobDouble(String colName) {
        return Double.parseDouble((String) rowValues.get(colName));
    }
}
