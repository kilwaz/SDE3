package application.test.action;

import application.test.TestParameter;
import application.test.TestStep;
import application.test.action.helpers.LoopedWebElement;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * This action lets keyboard inputs be pressed down, ideal for pretty tab or control+ keys
 */
public class KeyboardWebAction extends WebAction {

    private static Logger log = Logger.getLogger(KeyboardWebAction.class);

    public KeyboardWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestStep testStep = TestStep.create(TestStep.class);
        testStep.setParentResult(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter idElement = getTestCommand().getParameterByName("id");
        TestParameter xPathElement = getTestCommand().getParameterByName("xPath");
        TestParameter loopElement = getTestCommand().getParameterByName("loop");

        TestParameter keyToPress = getTestCommand().getParameterByName("press");

        WebElement testElement = null;
        if (idElement.exists()) { // Get the element via id
            testElement = getDriver().findElement(By.id(idElement.getParameterValue()));
        } else if (xPathElement.exists()) { // Get the element via xPath
            testElement = getDriver().findElement(By.xpath(xPathElement.getParameterValue()));
        } else if (loopElement.exists()) { // Get element via loop
            LoopedWebElement loopedWebElement = (LoopedWebElement) getLoopTracker().getLoop(loopElement.getParameterValue()).getCurrentLoopObject();
            testElement = loopedWebElement.getWebElement(getDriver());
        }

        if (testElement != null) {
            if (keyToPress.exists()) {
                if ("tab".equals(keyToPress.getParameterValue())) {
                    testElement.sendKeys(Keys.TAB);
                }
            }
        }

        testStep.save();
    }
}
