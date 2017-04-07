package sde.application.data;

import java.util.List;

public class SavableAttributeList {
    private String className;
    private String variableName;
    private List listVariable;

    public SavableAttributeList(String variableName, String className, List listVariable) {
        this.listVariable = listVariable;
        this.className = className;
        this.variableName = variableName;
    }

    public String getClassName() {
        return className;
    }

    public String getVariableName() {
        return variableName;
    }

    public List getVariable() {
        return listVariable;
    }
}
