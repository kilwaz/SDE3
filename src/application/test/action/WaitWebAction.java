package application.test.action;

import application.test.TestParameter;
import com.jayway.awaitility.Awaitility;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

/**
 * This action waits for a specific case to be true before continuing.
 *
 * A default timeout of 10 seconds is used.
 */
public class WaitWebAction extends WebAction {
    private static Logger log = Logger.getLogger(WaitWebAction.class);

    public WaitWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter elementToBeClickable = getParameterByPath("clickable::id");
        TestParameter elementToBePresentId = getParameterByPath("presence::id");
        TestParameter elementToBePresentXPath = getParameterByPath("presence::xPath");
        TestParameter javaScriptToBeTrue = getParameterByPath("frameLoaded::frame");
        TestParameter waitForTime = getParameterByPath("specificTime");
        TestParameter waitForRequests = getParameterByPath("finishAllRequests");

        try {
            if (elementToBeClickable.exists()) { // If it is specified, wait until this element is clickable
                WebDriverWait wait = new WebDriverWait(getDriver(), 10);
                wait.until(ExpectedConditions.elementToBeClickable(findElement(elementToBeClickable)));
            }
            if (elementToBePresentId.exists()) { // If it is specified, wait until this element is present
                WebDriverWait wait = new WebDriverWait(getDriver(), 10);
                wait.until(ExpectedConditions.presenceOfElementLocated(findElement(elementToBePresentId)));
            }
            if (elementToBePresentXPath.exists()) { // If it is specified, wait until this element is present
                WebDriverWait wait = new WebDriverWait(getDriver(), 10);
                wait.until(ExpectedConditions.presenceOfElementLocated(findElement(elementToBePresentXPath)));
            }
            if (javaScriptToBeTrue.exists()) { // If it is specified, wait for this javascript to be true
                WebDriverWait wait = new WebDriverWait(getDriver(), 10);
                wait.until((WebDriver driver) -> ((JavascriptExecutor) driver).executeScript("return $('#" + javaScriptToBeTrue.getParameterValue() + "').contents()[0].readyState").equals("complete"));
            }
            if (waitForTime.exists()) { // If it is specified, wait for this amount of time
                Thread.sleep(Long.parseLong(waitForTime.getParameterValue()));
            }
            if (waitForRequests.exists()) {
                Awaitility.await().atMost(Integer.parseInt(waitForRequests.getParameterValue()), TimeUnit.SECONDS).until(getHttpProxyServer().getWebProxyRequestManager().haveAllRequestsFinished());
            }

            refreshCurrentDocument();
        } catch (org.openqa.selenium.TimeoutException ex) {
            log.error("Element could not be found within the set time limit of 10 seconds", ex);
        } catch (InterruptedException ex) {
            log.error(ex);
        }
    }
}
