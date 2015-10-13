package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;

/**
 * This action runs javascript that is passed to it.
 */
public class JavascriptWebAction extends WebAction {

    private static Logger log = Logger.getLogger(JavascriptWebAction.class);

    public JavascriptWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter scriptToRun = getTestCommand().getParameterByPath("script");

        if (scriptToRun.exists()) { // If it is specified, wait for this javascript to be true
            ((JavascriptExecutor) getDriver()).executeScript(scriptToRun.getParameterValue());
        }

        refreshCurrentDocument();

        DataBank.saveTestStep(testStep);
    }
}
