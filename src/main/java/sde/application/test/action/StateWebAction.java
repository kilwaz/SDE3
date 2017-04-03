package sde.application.test.action;

import sde.application.test.ChangedElements;
import sde.application.test.TestParameter;
import sde.application.test.action.helpers.PageStateCapture;
import sde.application.test.action.helpers.Variable;
import sde.application.test.core.TestCase;
import org.apache.log4j.Logger;

public class StateWebAction extends WebAction {
    private static Logger log = Logger.getLogger(StateWebAction.class);

    public StateWebAction() {
    }

    public void performAction() {
        TestParameter saveStateName = getTestCommand().getParameterByName("save");
        TestParameter compareStateName = getTestCommand().getParameterByPath("compare");
        TestParameter withStateName = getTestCommand().getParameterByName("with");
        TestParameter resultVar = getTestCommand().getParameterByName("var");

        if (saveStateName.exists()) {
            PageStateCapture pageStateCapture = new PageStateCapture();
            pageStateCapture.setElementFrameTree(getDocumentTracker().getIFrameTree());
            pageStateCapture.setStateName(saveStateName.getParameterValue());
            pageStateCapture.capturePage(getDriver());
            getStateTracker().setState(pageStateCapture);
            TestCase testCase = getRunningTest().getTestCase();
            if (testCase != null) {
                testCase.storePageState(pageStateCapture);
            }
        } else if (compareStateName.exists() && withStateName.exists()) {
            PageStateCapture compareState = getStateTracker().getState(compareStateName.getParameterValue());
            PageStateCapture withState = getStateTracker().getState(withStateName.getParameterValue());

            if (compareState != null && withState != null) {
                ChangedElements changedElements = compareState.compare(withState);
                if (resultVar.exists()) {
                    getVariableTracker().setVariable(new Variable(resultVar.getParameterValue(), changedElements));
                }
                TestCase testCase = getRunningTest().getTestCase();
                if (testCase != null) {
                    testCase.setPageCaptures(compareState, withState);
                }
            }
        }
    }
}
