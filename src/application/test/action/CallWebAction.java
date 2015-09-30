package application.test.action;

import application.test.TestParameter;
import application.test.action.helpers.FunctionTracker;
import org.apache.log4j.Logger;

/**
 * This action is used to call another function within the script. This action jumps to the line after the referenced
 * function name.
 * <p>
 * After the function has finished the script will jump back to the next line after this to continue on.
 */
public class CallWebAction extends WebAction {

    private static Logger log = Logger.getLogger(CallWebAction.class);

    public CallWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter functionName = getParameterByPath("function");

        if (functionName.exists()) { // We only run if a function is provided
            Integer lineNumber = FunctionTracker.getFunction(functionName.getParameterValue()) + 1;

            // We add one to the line number as we really want to run what is inside the function, not the function itself.
            // One thing to note here is that the line counter has already been counted on for this line by the time this is
            // executed, therefore we need to backtrack once to get the real value for this current line.
            FunctionTracker.pushFunctionStack(getParentTestNode().getCurrentTestLine() - 1);
            getParentTestNode().setCurrentTestLine(lineNumber);
        }
    }
}
