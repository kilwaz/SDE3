package application.test.action;

import application.error.Error;
import application.test.TestParameter;
import application.test.action.helpers.LoopedWebElement;
import application.utils.SDEUtils;
import com.thoughtworks.selenium.webdriven.JavascriptLibrary;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.util.concurrent.TimeUnit;

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
        try {
            TestParameter elementId = getTestCommand().getParameterByPath("id");
            TestParameter valueToEnter = getTestCommand().getParameterByPath("value");
            TestParameter characterDelay = getTestCommand().getParameterByPath("characterDelay");
            TestParameter loopElement = getTestCommand().getParameterByName("loop");
            TestParameter eventToTrigger = getTestCommand().getParameterByName("event");
            TestParameter clearFirstText = getTestCommand().getParameterByName("clearFirst");
            TestParameter increaseBy = getTestCommand().getParameterByName("increaseBy");
            TestParameter decreaseBy = getTestCommand().getParameterByName("decreaseBy");

            // We only wait for 10 seconds for page loads, sometimes the click hangs forever otherwise
            getDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

            // Finds element
            WebElement testElement = null;
            if (elementId.exists()) { // Get the element via id
                testElement = getDriver().findElement(By.id(elementId.getParameterValue()));
            } else if (loopElement.exists()) { // Get element via loop
                LoopedWebElement loopedWebElement = (LoopedWebElement) getLoopTracker().getLoop(loopElement.getParameterValue()).getCurrentLoopObject();
                testElement = loopedWebElement.getWebElement(getDriver());
            }

            if (testElement != null && testElement.isDisplayed()) {
                String textToEnter = "";
                Boolean clearText = false;

                // Creates the text value we want
                if (valueToEnter.exists()) {
                    textToEnter = valueToEnter.getParameterValue();
                } else if (increaseBy.exists() || decreaseBy.exists()) {
                    String currentText = testElement.getAttribute("value");
                    currentText = SDEUtils.removeNonNumericCharacters(currentText); // Removes all non-numeric characters
                    Double textValue = Double.parseDouble(currentText);

                    if (increaseBy.exists()) {
                        textToEnter = Double.toString(textValue + SDEUtils.parseDouble(increaseBy.getParameterValue()));
                    } else if (decreaseBy.exists()) {
                        textToEnter = Double.toString(textValue - SDEUtils.parseDouble(decreaseBy.getParameterValue()));
                    }

                    clearText = true;
                }

                // Clear the text first
                if (clearFirstText.exists() || clearText) {
                    testElement.sendKeys(Keys.CONTROL + "a");
                    testElement.sendKeys(Keys.DELETE);
                }

                // This delays the input of the text to simulate as if the user were typing it themselves
                log.info("Entering value '" + textToEnter + "'");
                if (getRunningTest() != null && getRunningTest().getTestCase() != null) {
                    getRunningTest().getTestCase().log("Entering value '" + textToEnter + "'");
                }

                if (characterDelay.exists()) {
                    Long delay = Long.parseLong(characterDelay.getParameterValue());
                    char[] textSplit = textToEnter.toCharArray();

                    for (Character c : textSplit) {
                        testElement.sendKeys(c.toString());

                        // Pause the current thread for the time that we need
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else { // Update the text without a delay
                    testElement.sendKeys(textToEnter);
                }
                takeScreenshotOfElement(testElement);
            } else {
                log.info("Unable to enter value, element not found or not displayed");
                if (getRunningTest() != null && getRunningTest().getTestCase() != null) {
                    getRunningTest().getTestCase().log("Unable to enter value, element not found or not displayed");
                }
            }
            if (eventToTrigger.exists() && testElement != null) {
                JavascriptLibrary javascript = new JavascriptLibrary();
                javascript.callEmbeddedSelenium(getDriver(), "triggerEvent", testElement, eventToTrigger.getParameterValue());
            }
        } catch (Exception ex) {
            getTestCommand().setException(ex);
            Error.SELENIUM_INPUT_ACTION_NOT_FOUND.record().create(ex);
        } finally {
            // We sent the driver back to being unlimited timeout for page loads
            getDriver().manage().timeouts().pageLoadTimeout(-1, TimeUnit.SECONDS);
        }
    }
}
