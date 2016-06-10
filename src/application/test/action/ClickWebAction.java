package application.test.action;

import application.error.Error;
import application.test.TestParameter;
import application.test.action.helpers.LoopedWebElement;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.concurrent.TimeUnit;

/**
 * This action is used to click on an element that is specified by the script.
 * <p>
 * The element can be specified by either id or an xPath.
 * <p>
 * The element can also be retrieved from the current loop as well.
 */
public class ClickWebAction extends WebAction {

    private static Logger log = Logger.getLogger(ClickWebAction.class);

    // This class is used to click on an element
    public ClickWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        try {
            TestParameter xPathElement = getParameterByPath("xPath");
            TestParameter idElement = getParameterByPath("id");
            TestParameter loopElement = getParameterByName("loop");
            By testBy = null;

            // We only wait for 10 seconds for page loads, sometimes the click hangs forever otherwise
            getDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

            if (idElement.exists() || xPathElement.exists()) {
                if (xPathElement.exists()) {
                    testBy = findElement(xPathElement);
                } else if (idElement.exists()) {
                    testBy = findElement(idElement);
                }

                if (testBy != null) {
                    WebElement testElement = getDriver().findElement(testBy);
                    processElement(testElement);
                }
            } else if (loopElement.exists()) {
                WebElement loopedElement = null;
                LoopedWebElement loopedWebElement = (LoopedWebElement) getLoopTracker().getLoop(loopElement.getParameterValue()).getCurrentLoopObject();
                if (loopedWebElement != null) {
                    loopedElement = loopedWebElement.getWebElement(getDriver());
                }

                processElement(loopedElement);
            } else {
                log.info("No element exists to be clicked");
                if (getRunningTest() != null && getRunningTest().getTestCase() != null) {
                    getRunningTest().getTestCase().log("No element exists to be clicked");
                }
            }
        } catch (Exception ex) {
            getTestCommand().setException(ex);
            Error.SELENIUM_CLICK_ACTION_NOT_FOUND.record().create(ex);
        } finally {
            // We sent the driver back to being unlimited timeout for page loads
            getDriver().manage().timeouts().pageLoadTimeout(-1, TimeUnit.SECONDS);
        }
    }

    /**
     * Processes the element, in this case we need to click the element once we have found it.
     *
     * @param webElement The Selenium WebElement we are going to click.
     */
    private void processElement(WebElement webElement) {
        if (webElement != null) {
            takeScreenshotOfElement(webElement);
            if (webElement.isDisplayed()) {
                webElement.click();
            } else {
                Error.SELENIUM_CLICK_ACTION_NOT_FOUND.record().additionalInformation(webElement.toString()).create();
            }
        }
    }
}
