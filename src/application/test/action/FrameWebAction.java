package application.test.action;

import application.error.Error;
import application.test.TestParameter;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * This action switches the current frame that is being referenced as part of the script.
 * <p>
 * The frame can be specified by using the id of the element.
 * <p>
 * select::default can be used to switch to the top frame of the browser.
 */
public class FrameWebAction extends WebAction {
    private static Logger log = Logger.getLogger(FrameWebAction.class);

    public FrameWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter frameToSelect = getParameterByPath("select");

        By specifiedBy = specifiedBy();
        if (specifiedBy != null) { // Wait if we have something we can wait for
            try {
                WebDriverWait wait = new WebDriverWait(getDriver(), 10);
                wait.until(ExpectedConditions.presenceOfElementLocated(specifiedBy));
//                wait.until(webDriver -> {
//                    WebElement webElement = webDriver.findElement(specifiedBy);
//                    return webElement != null;
//                });
            } catch (org.openqa.selenium.TimeoutException ex) {
                Error.SELENIUM_FRAME_NOT_FOUND.record().additionalInformation("Frame '" + specifiedBy.toString() + "' could not be located in time").create(ex);
            }

            try {
                WebElement frameElement = specifiedElement();
                if (frameElement != null) {
                    try {
                        getDocumentTracker().addIFrameChange(frameElement.getAttribute("id"));
                        getDriver().switchTo().frame(frameElement);
                    } catch (NoSuchFrameException ex) {
                        Error.SELENIUM_FRAME_NOT_FOUND.record().additionalInformation("Frame " + specifiedBy.toString() + " is not a frame").create(ex);
                    }
                } else {
                    Error.SELENIUM_FRAME_NOT_FOUND.record().additionalInformation("Frame " + specifiedBy.toString() + " was null").create();
                }
            } catch (NoSuchElementException ex) {
                getTestCommand().setException(ex);
                Error.SELENIUM_FRAME_NOT_FOUND.record().additionalInformation("Frame '" + specifiedBy.toString() + "' could not be located").create(ex);
            }
        }

        if (frameToSelect.exists()) {
            getDriver().switchTo().defaultContent();
            getDocumentTracker().setToDefaultFrame();
        }

        getDocumentTracker().refreshCurrentDocument();
    }
}
