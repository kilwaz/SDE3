package application.test.structure;

import application.test.TestCommand;
import application.test.TestParameter;

import java.util.ArrayList;
import java.util.List;

public class BaseTarget {
    private static List<String> targetTypes = new ArrayList<>();

    static {
        targetTypes.add("id");
        targetTypes.add("name");
        targetTypes.add("xPath");
        targetTypes.add("variable");
    }

    private String id = null;
    private String xPath = null;
    private String name = null;
    private String variable = null;

    public BaseTarget(TestCommand testCommand) {
        TestParameter idParam = testCommand.getParameterByName("id");
        TestParameter xPathParam = testCommand.getParameterByName("xPath");
        TestParameter nameParam = testCommand.getParameterByName("name");
        //TestParameter variableParam = testCommand.getParameterByName("id");

        if (idParam.exists()) this.id = idParam.getParameterValue();
        if (xPathParam.exists()) this.xPath = xPathParam.getParameterValue();
        if (nameParam.exists()) this.name = nameParam.getParameterValue();
    }

    public static List<String> getNamedListOfTargetTypes() {
        return targetTypes;
    }

    public String getId() {
        return id;
    }

    public String getXPath() {
        return xPath;
    }

    public String getVariable() {
        return variable;
    }

    public String getName() {
        return name;
    }

    public String getTypeInUse() {
        if (id != null) {
            return targetTypes.get(0);
        } else if (name != null) {
            return targetTypes.get(1);
        } else if (xPath != null) {
            return targetTypes.get(2);
        } else if (variable != null) {
            return targetTypes.get(3);
        }
        return "";
    }

    public String getValueInUse() {
        if (id != null) {
            return getId();
        } else if (name != null) {
            return getName();
        } else if (xPath != null) {
            return getXPath();
        } else if (variable != null) {
            return getVariable();
        }
        return "";
    }

    public void setValueInUse(String value) {
        if (id != null) {
            id = value;
        } else if (name != null) {
            name = value;
        } else if (xPath != null) {
            xPath = value;
        } else if (variable != null) {
            variable = value;
        }
    }

    public Boolean isSet() {
        return id != null || name != null || xPath != null || variable != null;
    }
}
