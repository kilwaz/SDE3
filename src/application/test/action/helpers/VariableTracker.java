package application.test.action.helpers;

import java.util.HashMap;

/**
 * The Variable Tracker is used to keep track of all the current variables within a current script running through a
 * {@link application.node.implementations.TestNode}.
 * <p>
 * The {@link application.test.action.SetWebAction} is used to set the variable.
 */

public class VariableTracker {
    private static HashMap<String, Variable> variables = new HashMap<>();

    /**
     * @param ref Variable reference to find.
     * @return Returned found variable, or null if one isn't found.
     */
    public static Variable getVariable(String ref) {
        return variables.get(ref);
    }

    /**
     * @param ref Variable reference to remove
     */
    public static void removeVariable(String ref) {
        variables.remove(ref);
    }

    /**
     * @param variable Variable to save.
     */
    public static void setVariable(Variable variable) {
        variables.put(variable.getVariableName(), variable);
    }
}
