package application.data;

public class SavableAttribute {
    private String className;
    private String variableName;
    private Object variable;

    public SavableAttribute(String variableName, String className, Object variable) {
        this.variable = variable;
        this.className = className;
        this.variableName = variableName;
    }

    public String getClassName() {
        return className;
    }

    public String getVariableName() {
        return variableName;
    }

    public Object getVariable() {
        return variable;
    }
}
