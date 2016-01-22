package application.test.action;

import application.test.ChangedElements;
import application.test.TestParameter;
import application.test.TestStep;
import application.test.action.helpers.PageStateCapture;
import application.test.action.helpers.Variable;
import org.apache.log4j.Logger;

public class StateWebAction extends WebAction {
    private static Logger log = Logger.getLogger(StateWebAction.class);

    public StateWebAction() {
    }

    public void performAction() {
        TestStep testStep = TestStep.create(TestStep.class);
        testStep.setParentResult(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter saveStateName = getTestCommand().getParameterByName("save");
        TestParameter compareStateName = getTestCommand().getParameterByPath("compare");
        TestParameter withStateName = getTestCommand().getParameterByName("with");
        TestParameter resultVar = getTestCommand().getParameterByName("var");

        if (saveStateName.exists()) {
            PageStateCapture pageStateCapture = new PageStateCapture("default", saveStateName.getParameterValue());
            pageStateCapture.capturePage(getDriver());
            getStateTracker().setState(pageStateCapture);
        } else if (compareStateName.exists() && withStateName.exists()) {
            PageStateCapture compareState = getStateTracker().getState(compareStateName.getParameterValue());
            PageStateCapture withState = getStateTracker().getState(withStateName.getParameterValue());

            if (compareState != null && withState != null) {
                ChangedElements changedElements = compareState.compare(withState);
                if (resultVar.exists()) {
                    getVariableTracker().setVariable(new Variable(resultVar.getParameterValue(), changedElements));
                }
            }
        }

        testStep.save();
    }
}
