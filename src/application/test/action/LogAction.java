package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class LogAction extends ActionControl {

    private static Logger log = Logger.getLogger(LogAction.class);

    // This class is used to input a value into an element
    public LogAction() {
    }

    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter idElement = getTestCommand().getParameterByName("id");
        TestParameter xPathElement = getTestCommand().getParameterByName("xPath");
        TestParameter message = getTestCommand().getParameterByName("message");
        if (message != null) {
            log.info(message.getParameterValue());
            testStep.setTestString(getTestCommand().getRawCommand());
        } else if (idElement != null || xPathElement != null) {
            By testBy = null;

            if (xPathElement != null) {
                testBy = findElement(xPathElement);
            } else if (idElement != null) {
                testBy = findElement(idElement);
            }

            if (testBy != null) {
                WebElement testElement = getDriver().findElement(testBy);
                if (testElement != null) {
                    takeScreenshotOfElement(testStep, testElement);
                    testStep.setTestString(getTestCommand().getRawCommand());
                    log.info(testElement);
                }
            }
        }

        DataBank.saveTestStep(testStep);
    }
}
