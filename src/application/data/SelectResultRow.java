package application.data;


import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.SQLException;
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

    public InputStream getBlobInputStream(String colName) {
        try {
            return ((Blob) rowValues.get(colName + "-Blob")).getBinaryStream();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BigDecimal getBigDecimal(String colName) {
        return (BigDecimal) rowValues.get(colName);
    }

    public BigInteger getBigInt(String colName) {
        return (BigInteger) rowValues.get(colName);
    }

    public String getBlobString(String colName) {
        return (String) rowValues.get(colName + "-String");
    }

    public Integer getBlobInt(String colName) {
        return Integer.parseInt((String) rowValues.get(colName + "-String"));
    }

    public Double getBlobDouble(String colName) {
        return Double.parseDouble((String) rowValues.get(colName + "-String"));
    }
}
