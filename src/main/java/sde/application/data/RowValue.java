package sde.application.data;

public class RowValue {
    private String name;
    private Object value;

    public RowValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
