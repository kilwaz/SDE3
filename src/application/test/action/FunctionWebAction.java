package application.test.action;

import application.test.TestParameter;
import application.test.action.helpers.FunctionTracker;
import org.apache.log4j.Logger;

/**
 * This action is used to define a function within a script.
 * <p>
 * It is used as a reference point in the script that can be called by the Call action.
 * <p>
 * After the function is finished the script will return to the Call action that called it.
 */
public class FunctionWebAction extends WebAction {

    private static Logger log = Logger.getLogger(FunctionWebAction.class);

    public FunctionWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter functionStart = getParameterByPath("start");
        TestParameter functionEnd = getParameterByPath("end");

        if (functionStart.exists()) {
            // If we reach a function start we need to skip to the end of the function, functions can only be run via calling them
            FunctionTracker.setIsSkippingFunction(true);
            FunctionTracker.setFunctionReference(functionStart.getParameterValue());
        }

        if (functionEnd.exists()) {
            // If we get to the end of our function then we need to back to the original call
            Integer callingLineNumber = FunctionTracker.popFunctionStack() + 1;
            getParentTestNode().setCurrentTestLine(callingLineNumber);
        }
    }
}
