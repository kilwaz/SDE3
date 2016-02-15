package application.test.action;

import application.error.Error;
import application.test.TestParameter;
import application.test.TestStep;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;

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
        TestStep testStep = TestStep.create(TestStep.class);
        testStep.setParentResult(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter scriptToRun = getTestCommand().getParameterByPath("script");

        if (scriptToRun.exists()) { // If it is specified, wait for this javascript to be true
            try {
                ((JavascriptExecutor) getDriver()).executeScript(scriptToRun.getParameterValue());
            } catch (WebDriverException ex) {
                Error.SELENIUM_JAVASCRIPT_FAILED.record().additionalInformation("Script:- " + scriptToRun.getParameterValue()).create(ex);
            }
        }

        refreshCurrentDocument();
        testStep.save();
    }
}
