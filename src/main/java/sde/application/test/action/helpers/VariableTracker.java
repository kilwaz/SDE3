package sde.application.test.action.helpers;

import sde.application.node.implementations.TestNode;
import sde.application.test.action.SetWebAction;

import java.util.HashMap;

/**
 * The Variable Tracker is used to keep track of all the current variables within a current script running through a
 * {@link TestNode}.
 * <p>
 * The {@link SetWebAction} is used to set the variable.
 */

public class VariableTracker {
    private HashMap<String, Variable> variables = new HashMap<>();

    /**
     * @param ref Variable reference to find.                                                    l
     * @return Returned found variable, or null if one isn't found.
     */
    public Variable getVariable(String ref) {
        return variables.get(ref);
    }

    /**
     * @param ref Variable reference to remove
     */
    public void removeVariable(String ref) {
        variables.remove(ref);
    }

    /**
     * @param variable Variable to save.
     */
    public void setVariable(Variable variable) {
        variables.put(variable.getVariableName(), variable);
    }
}
