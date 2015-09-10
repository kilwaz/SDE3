package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;

public class JavascriptAction extends ActionControl {

    private static Logger log = Logger.getLogger(JavascriptAction.class);

    public JavascriptAction() {
    }

    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter scriptToRun = getTestCommand().getParameterByPath("script");

        if (scriptToRun != null) { // If it is specified, wait for this javascript to be true
            ((JavascriptExecutor) getDriver()).executeScript(scriptToRun.getParameterValue());
        }

        DataBank.saveTestStep(testStep);
    }
}
