package application.node.objects;

import application.data.model.DatabaseObject;
import application.node.implementations.InputNode;

public class Input extends DatabaseObject {
    private String variableName;
    private String variableValue;
    private InputNode parent;

    public Input(Integer id, String variableName, String variableValue, InputNode parent) {
        super(id);
        this.variableName = variableName;
        this.variableValue = variableValue;
        this.parent = parent;
    }

    public Input(Input copyInput, InputNode parent) {
        super(-1);
        this.variableName = copyInput.getVariableName();
        this.variableValue = copyInput.getVariableValue();
        this.parent = parent;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }

    public String getVariableValueLimited() {
        if (variableValue.length() > 100) {
            return variableValue.substring(0, 99);
        }
        return variableValue;
    }

    public Integer getParentId() {
        return parent.getId();
    }

    public InputNode getParent() {
        return parent;
    }

    public void setParent(InputNode parent) {
        this.parent = parent;
    }
}
