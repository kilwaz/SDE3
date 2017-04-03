package sde.application.data;

import sde.application.data.model.DatabaseObject;
import sde.application.data.model.dao.SavableAttributeDAO;
import sde.application.node.design.DrawableNode;

public class SavableAttribute extends DatabaseObject {
    private String className;
    private String variableName;
    private Object variable;
    private DrawableNode parentNode;

    public SavableAttribute() {
        super();
    }

    public void init(String variableName, String className, Object variable, DrawableNode parent) {
        this.variable = variable;
        this.className = className;
        this.variableName = variableName;
        this.parentNode = parent;
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

    public String getParentUuid() {
        if (parentNode != null) {
            return parentNode.getUuidString();
        }
        return null;
    }

    public void setParentNode(DrawableNode parentNode) {
        this.parentNode = parentNode;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setVariable(Object variable) {
        this.variable = variable;
    }

    public void save() {
        SavableAttributeDAO savableAttributeDAO = new SavableAttributeDAO();
        savableAttributeDAO.deletePreviousRecord(this);
        super.save();
    }
}
