package sde.application.test.action;

import sde.application.test.TestParameter;
import org.apache.log4j.Logger;

public class TestWebAction extends WebAction {
    private static Logger log = Logger.getLogger(TestWebAction.class);

    public TestWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter outputFile = getTestCommand().getParameterByName("outputFile");

        if (outputFile.exists()) {
            getRunningTest().setFileOutputPath(outputFile.getParameterValue());
        }
    }
}
