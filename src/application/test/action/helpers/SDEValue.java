package application.test.action.helpers;

import application.error.Error;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SDEValue {
    private String stringValue = null;
    private Integer decimalPlaces = null;
    private Boolean isNumber = false;

    public SDEValue(String stringValue) {
        this.stringValue = stringValue;
        this.isNumber = false;
    }

    public SDEValue(Double doubleValue) {
        this.stringValue = doubleValue.toString();
        this.isNumber = true;
    }

    public SDEValue roundTo(Integer decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
        return this;
    }

    public SDEValue isNumber() {
        this.isNumber = true;
        return this;
    }

    public String asString() {
        return stringValue;
    }

    public Double asDouble() {
        Double doubleValue = 0d;
        if (isNumber) {
            try {
                String numericOnly = stringValue.replaceAll("[^\\d.]", ""); // Alpha-numeric characters only
                doubleValue = Double.parseDouble(numericOnly);
            } catch (NumberFormatException ex) {
                Error.PARSE_DOUBLE_FAILED.record().hideStackInLog().create(ex);
                return 0d;
            }
        }

        if (decimalPlaces != null) {
            return round(doubleValue, decimalPlaces);
        } else {
            return doubleValue;
        }
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

