package application.test.action;

import application.error.Error;
import application.error.RecordedError;
import application.net.proxy.WebProxyRequest;
import application.test.TestParameter;
import com.jayway.awaitility.Awaitility;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * This action waits for a specific case to be true before continuing.
 * <p>
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
        TestParameter waitForRandomTime = getParameterByPath("randomTime");
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
                wait.withTimeout(10, TimeUnit.SECONDS).until((WebDriver driver) -> ((JavascriptExecutor) driver).executeScript("var frame = document.getElementById(\"" + javaScriptToBeTrue.getParameterValue() + "\");" +
                        "if(frame != null) {" +
                        "if(frame.contentDocument != null){" +
                        "       return frame.contentDocument.readyState;" +
                        "   } else {" +
                        "       return false" +
                        "   }" +
                        "} else {" +
                        "   return false" +
                        "};").equals("complete"));
            }
            if (waitForTime.exists()) { // If it is specified, wait for this amount of time
                Thread.sleep(Long.parseLong(waitForTime.getParameterValue()));
            }
            if (waitForRandomTime.exists()) { // Waits for a random time between two values
                TestParameter waitForRandomTimeMin = getParameterByPath("min");
                TestParameter waitForRandomTimeMax = getParameterByPath("max");

                Integer min = 0;
                Integer max = 0;
                if (waitForRandomTimeMin.exists()) {
                    min = Integer.parseInt(waitForRandomTimeMin.getParameterValue());
                }
                if (waitForRandomTimeMax.exists()) {
                    max = Integer.parseInt(waitForRandomTimeMax.getParameterValue());
                }

                Random ran = new Random();
                Integer waitTime = ran.nextInt((1 + max) - min) + min;
                log.info("Time to wait = " + waitTime + " (" + min + "/" + max + ")");
                Thread.sleep(waitTime);
            }
            if (waitForRequests.exists()) {
                Awaitility.await().atMost(Integer.parseInt(waitForRequests.getParameterValue()), TimeUnit.MILLISECONDS).until(getHttpProxyServer().getWebProxyRequestManager().haveAllRequestsFinished());
            }

            getDocumentTracker().refreshCurrentDocument();
        } catch (UnhandledAlertException ex) { // If a popup is blocking a wait command close it
            log.info("Auto closing alert popup");
            Alert alert = getDriver().switchTo().alert();
            alert.accept();
        } catch (org.openqa.selenium.TimeoutException ex) {
            getTestCommand().setException(ex);
            Error.WAIT_ACTION_TIMEOUT.record().additionalInformation("10 second limit").create(ex);
        } catch (InterruptedException | WebDriverException ex) {
            getTestCommand().setException(ex);
            Error.WAIT_ACTION_INTERRUPT.record().create(ex);
        } catch (com.jayway.awaitility.core.ConditionTimeoutException ex) {
            getTestCommand().setException(ex);
            RecordedError requestWaitError = Error.WAIT_ACTION_TIMEOUT.record().additionalInformation("Wait for all requests timed out");
            for (WebProxyRequest webProxyRequest : getHttpProxyServer().getWebProxyRequestManager().getActiveRequests().values()) {
                requestWaitError.additionalInformation("Active request: " + webProxyRequest.getRequestURL());
            }
            requestWaitError.create(ex);
        } catch (Exception ex) {
            Error.WAIT_ACTION_TIMEOUT.record().additionalInformation("Unexpected exception").create(ex);
        }
    }
}
