package application.test.action.helpers;

import application.error.Error;
import application.utils.SDEUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class SDEValue {
    private String stringValue = null;
    private Integer decimalPlaces = null;
    private Boolean isNumber = false;
    private Boolean isDate = false;
    private Boolean negativesHaveBrackets = false;

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

    public SDEValue isDate() {
        this.isDate = true;
        return this;
    }

    public String asString() {
        if (isNumber) {
            // Format the string to be the correct number of decimal places if it has been specified
            if (decimalPlaces != null) {
                DecimalFormat df = new DecimalFormat("#." + StringUtils.repeat("0", decimalPlaces));
                return df.format(asDouble());
            } else {
                return asDouble().toString();
            }
        } else {
            return stringValue;
        }
    }

    public SDEValue hasNegativesBrackets() {
        this.negativesHaveBrackets = true;
        return this;
    }

    public Double asDouble() {
        Double doubleValue = 0d;
        if (isNumber) {
            try {
                Boolean isNegative = false; // Decide if the value is negative but with brackets
                if (negativesHaveBrackets) {
                    if (stringValue.startsWith("(") && stringValue.endsWith(")")) {
                        isNegative = true;
                    }
                }
                String numericOnly = SDEUtils.removeNonNumericCharacters(stringValue); // Alpha-numeric and dash characters only
                doubleValue = SDEUtils.parseDouble(numericOnly);
                if (isNegative && negativesHaveBrackets) { // If it was a negative value with brackets, deal with it here
                    doubleValue = -doubleValue;
                }
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

    public DateTime asDate(String pattern) {
        try {
            DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern(pattern);
            return dateStringFormat.parseDateTime(stringValue);
        } catch (IllegalArgumentException ex) {
            Error.PARSE_DATE_FAILED.record().additionalInformation("Pattern = " + pattern).additionalInformation("Value = '" + stringValue + "'").create(ex);
        }

        return null;
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

