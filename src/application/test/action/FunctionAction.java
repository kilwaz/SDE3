package application.test.action;

import application.test.TestParameter;
import application.test.action.helpers.FunctionTracker;
import org.apache.log4j.Logger;

public class FunctionAction extends ActionControl {

    private static Logger log = Logger.getLogger(FunctionAction.class);

    public FunctionAction() {
    }

    public void performAction() {
        TestParameter functionStart = getTestCommand().getParameterByPath("start");
        TestParameter functionEnd = getTestCommand().getParameterByPath("end");

        if (functionStart != null) {
            // If we reach a function start we need to skip to the end of the function, functions can only be run via calling them
            FunctionTracker.setIsSkippingFunction(true);
            FunctionTracker.setFunctionReference(functionStart.getParameterValue());
        }

        if (functionEnd != null) {
            // If we get to the end of our function then we need to back to the original call
            Integer callingLineNumber = FunctionTracker.popFunctionStack() + 1;
            getParentTestNode().setCurrentTestLine(callingLineNumber);
        }
    }
}
