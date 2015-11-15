package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import application.test.action.helpers.LoopTracker;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * This action finds an input box on the screen and inputs some text to it.
 *
 * For a more user simulated approach you can have the characters typed in with a specified delay
 */
public class InputWebAction extends WebAction {
    private static Logger log = Logger.getLogger(InputWebAction.class);

    public InputWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter elementId = getTestCommand().getParameterByPath("id");
        TestParameter elementXPath = getTestCommand().getParameterByPath("xPath");
        TestParameter valueToEnter = getTestCommand().getParameterByPath("value");
        TestParameter characterDelay = getTestCommand().getParameterByPath("characterDelay");
        TestParameter loopElement = getTestCommand().getParameterByName("loop");

        WebElement testElement = null;
        if (elementId != null) { // Get the element via id
            testElement = getDriver().findElement(By.id(elementId.getParameterValue()));
        } else if (loopElement != null) { // Get element via loop
            testElement = LoopTracker.getLoop(loopElement.getParameterValue()).getCurrentLoopWebElement().getWebElement(getDriver());
        }

        if (valueToEnter.exists() && testElement != null) {
            // This delays the input of the text to simulate as if the user were typing it themselves
            if (characterDelay.exists()) {
                Long delay = Long.parseLong(characterDelay.getParameterValue());
                char[] textSplit = valueToEnter.getParameterValue().toCharArray();

                for (Character c : textSplit) {
                    testElement.sendKeys(c.toString());

                    // Pause the current thread for the time that we need
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                testElement.sendKeys(valueToEnter.getParameterValue());
            }
            takeScreenshotOfElement(testStep, testElement);
            testStep.setTestString(getTestCommand().getRawCommand());
        }

        testStep.save();
    }
}
