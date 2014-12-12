package application.node.objects;

import application.node.implementations.InputNode;

public class Input {
    private Integer id = -1;
    private String variableName;
    private String variableValue;
    private InputNode parent;

    public Input(Integer id, String variableName, String variableValue, InputNode parent) {
        this.id = id;
        this.variableName = variableName;
        this.variableValue = variableValue;
        this.parent = parent;
    }

    public Input(Input copyInput, InputNode parent) {
        this.variableName = copyInput.getVariableName();
        this.variableValue = copyInput.getVariableValue();
        this.parent = parent;
        this.id = -1;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public InputNode getParent() {
        return parent;
    }

    public void setParent(InputNode parent) {
        this.parent = parent;
    }
}
