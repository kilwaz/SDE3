package application.test.action;

import application.test.TestParameter;
import application.test.TestStep;
import com.thoughtworks.selenium.webdriven.JavascriptLibrary;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * This action finds an input box on the screen and inputs some text to it.
 * <p>
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
        TestStep testStep = TestStep.create(TestStep.class);
        testStep.setParentResult(getTestResult());

        getTestResult().addTestStep(testStep);

        TestParameter elementId = getTestCommand().getParameterByPath("id");
        TestParameter valueToEnter = getTestCommand().getParameterByPath("value");
        TestParameter characterDelay = getTestCommand().getParameterByPath("characterDelay");
        TestParameter loopElement = getTestCommand().getParameterByName("loop");
        TestParameter eventToTrigger = getTestCommand().getParameterByName("event");
        TestParameter clearFirstText = getTestCommand().getParameterByName("clearFirst");

        WebElement testElement = null;
        if (elementId.exists()) { // Get the element via id
            testElement = getDriver().findElement(By.id(elementId.getParameterValue()));
        } else if (loopElement.exists()) { // Get element via loop
            testElement = getLoopTracker().getLoop(loopElement.getParameterValue()).getCurrentLoopWebElement().getWebElement(getDriver());
        }

        if (valueToEnter.exists() && testElement != null) {
            if (clearFirstText.exists()) {
                testElement.sendKeys(Keys.CONTROL + "a");
                testElement.sendKeys(Keys.DELETE);
            }

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
        } else if (eventToTrigger.exists() && testElement != null) {
            JavascriptLibrary javascript = new JavascriptLibrary();
            javascript.callEmbeddedSelenium(getDriver(), "triggerEvent", testElement, eventToTrigger.getParameterValue());
        }

        testStep.save();
    }
}
