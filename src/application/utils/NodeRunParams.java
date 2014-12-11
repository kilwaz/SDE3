package application.utils;

import java.util.HashMap;

public class NodeRunParams {
    private HashMap<String, Object> params = new HashMap<>();
    private Object oneTimeVariable = null;

    public NodeRunParams() {

    }

    public void addVariable(String name, Object obj) {
        params.put(name, obj);
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }

    public Object getOneTimeVariable() {
        return oneTimeVariable;
    }

    public void setOneTimeVariable(Object oneTimeVariable) {
        this.oneTimeVariable = oneTimeVariable;
    }
}
