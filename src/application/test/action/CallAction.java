package application.test.action;

import application.test.TestParameter;
import application.test.action.helpers.FunctionTracker;
import org.apache.log4j.Logger;

public class CallAction extends ActionControl {

    private static Logger log = Logger.getLogger(CallAction.class);

    public CallAction() {
    }

    public void performAction() {
        TestParameter functionName = getTestCommand().getParameterByPath("function");

        Integer lineNumber = FunctionTracker.getFunction(functionName.getParameterValue()) + 1;

        // We add one to the line number as we really want to run what is inside the function, not the function itself.
        // One thing to note here is that the line counter has already been counted on for this line by the time this is
        // executed, therefore we need to backtrack once to get the real value for this current line.
        FunctionTracker.pushFunctionStack(getParentTestNode().getCurrentTestLine() - 1);
        getParentTestNode().setCurrentTestLine(lineNumber);
    }
}
