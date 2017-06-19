package sde.application.data;


import org.apache.log4j.Logger;
import sde.application.error.Error;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.SQLException;

public class SelectResultRow {
    private static Logger log = Logger.getLogger(SelectResultRow.class);
    private RowValues rowValues = new RowValues();

    public SelectResultRow() {
    }

    public void addColumn(String colName, Object colValue) {
        rowValues.addValue(new RowValue(colName, colValue));
    }

    public Object getColumnObject(String colName) {
        return rowValues.get(colName).getValue();
    }

    public String getString(Integer columnNumber) {
        return (String) rowValues.get(columnNumber).getValue();
    }

    public Integer getInt(String colName) {
        // null = 0
        // false = 0
        // true = 1
        // int = int

        // Handled for both boolean and integer return types
        if (rowValues.get(colName) != null) {
            Object value = rowValues.get(colName).getValue();
            if (value instanceof Boolean) {
                return (Boolean) value ? 1 : 0;
            } else if (value instanceof Integer) {
                return (Integer) value;
            }
        }

        return 0;
    }

    public String getString(String colName) {
        return (String) rowValues.get(colName).getValue();
    }

    public Double getDouble(String colName) {
        return (Double) rowValues.get(colName).getValue();
    }

    public Boolean getBoolean(String colName) {
        // null = false
        // 1 = true
        // 0 = false

        // Handled for both boolean and integer return types
        if (rowValues.get(colName) != null) {
            Object value = rowValues.get(colName).getValue();
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else if (value instanceof Integer) {
                return (Integer) value == 1;
            }
        }
        return false;
    }

    public InputStream getBlobInputStream(String colName) {
        try {
            return ((Blob) rowValues.get(colName + "-Blob").getValue()).getBinaryStream();
        } catch (NullPointerException ex) {
            Error.SQL_BLOB.record().additionalInformation("Nothing returned from column lookup: " + colName + "-Blob").create(ex);
        } catch (SQLException ex) {
            Error.SQL_BLOB.record().create(ex);
        }
        return null;
    }

    public BigDecimal getBigDecimal(String colName) {
        return (BigDecimal) rowValues.get(colName).getValue();
    }

    public BigInteger getBigInt(String colName) {
        return (BigInteger) rowValues.get(colName).getValue();
    }

    public Long getLong(String colName) {
        return (Long) rowValues.get(colName).getValue();
    }

    public String getBlobString(String colName) {
        return (String) rowValues.get(colName + "-String").getValue();
    }

    public Integer getBlobInt(String colName) {
        return Integer.parseInt((String) rowValues.get(colName + "-String").getValue());
    }

    public Double getBlobDouble(String colName) {
        return Double.parseDouble((String) rowValues.get(colName + "-String").getValue());
    }
}
