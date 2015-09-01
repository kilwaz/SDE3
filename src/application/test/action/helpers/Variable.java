package application.test.action.helpers;

/**
 * This is a simple class to keep a variable reference and value pair.
 */
public class Variable {
    private String variableName;
    private String variableValue;

    /**
     * @param variableName  String variable reference name we want to use.
     * @param variableValue String variable value.
     */
    public Variable(String variableName, String variableValue) {
        this.variableName = variableName;
        this.variableValue = variableValue;
    }

    /**
     * @return Gets Variable Name.
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * @return Gets Variable Value.
     */
    public String getVariableValue() {
        return variableValue;
    }
}
