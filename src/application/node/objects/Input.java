package application.node.objects;

import application.data.model.DatabaseObject;
import application.node.implementations.InputNode;

import java.util.UUID;

public class Input extends DatabaseObject {
    private String variableName;
    private String variableValue;
    private InputNode parentNode;

    public Input(UUID uuid, String variableName, String variableValue, InputNode parent) {
        super(uuid);
        this.variableName = variableName;
        this.variableValue = variableValue;
        this.parentNode = parent;
    }

    public Input(Input copyInput, InputNode parent) {
        this.variableName = copyInput.getVariableName();
        this.variableValue = copyInput.getVariableValue();
        this.parentNode = parent;
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

    public String getParentUuid() {
        if(parentNode != null){
            return parentNode.getUuidString();
        }
        return null;
    }

    public void setParent(InputNode parent) {
        this.parentNode = parent;
    }
}
