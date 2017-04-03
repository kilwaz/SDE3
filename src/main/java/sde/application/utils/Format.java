package sde.application.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

public class Format {
    private Integer decimalPlaces = null;
    private Double value = null;
    private String pattern = null;
    private Boolean commaSeparator = false;

    public Format() {
    }

    public static Format get() {
        return new Format();
    }

    public Format value(String value) {
        this.value = Double.parseDouble(value);
        return this;
    }

    public Format value(Double value) {
        this.value = value;
        return this;
    }

    public Format value(Integer value) {
        this.value = value.doubleValue();
        return this;
    }

    public Format decimalPlaces(Integer decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
        return this;
    }

    public Format withCommaSeparator() {
        this.commaSeparator = true;
        return this;
    }

    public Format pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public String asString() {
        if (value != null) {
            String pattern = this.pattern;

            // If the pattern isn't explicitly set then create it based on what else has been entered
            if (pattern == null) {
                pattern = "#";

                if (decimalPlaces != null) {
                    pattern = pattern + "." + StringUtils.repeat("0", decimalPlaces);
                }

                if (commaSeparator) {
                    pattern = "#,##" + pattern;
                }
            }

            DecimalFormat df = new DecimalFormat(pattern);
            return df.format(value);
        }
        return "";
    }
}
