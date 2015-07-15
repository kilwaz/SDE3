package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.concurrent.TimeUnit;

public class ClickAction extends ActionControl {

    private static Logger log = Logger.getLogger(ClickAction.class);

    // This class is used to click on an element
    public ClickAction() {
    }

    public void performAction() {
        try {
            TestStep testStep = DataBank.createNewTestStep(getTestResult());
            getTestResult().addTestStep(testStep);

            TestParameter xPathElement = getTestCommand().getParameterByPath("xPath");
            TestParameter idElement = getTestCommand().getParameterByPath("id");
            By testBy = null;

            if (xPathElement != null) {
                testBy = findElement(xPathElement);
            } else if (idElement != null) {
                testBy = findElement(idElement);
            }

            // We only wait for 10 seconds for page loads, sometimes the click hangs forever otherwise
            getDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

            if (testBy != null) {
                WebElement testElement = getDriver().findElement(testBy);
                if (testElement != null) {
                    takeScreenshotOfElement(testStep, testElement);
                    testStep.setTestString(getTestCommand().getRawCommand());
                    testElement.click();
                }
            }

            // We sent the driver back to being unlimited timeout for page loads
            getDriver().manage().timeouts().pageLoadTimeout(-1, TimeUnit.SECONDS);

            DataBank.saveTestStep(testStep);
        } catch (Exception ex) {
            log.error("Click Action is failing", ex);
        }
    }
}
