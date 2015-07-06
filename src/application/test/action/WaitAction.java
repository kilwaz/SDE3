package application.test.action;

import application.test.TestParameter;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WaitAction extends ActionControl {
    private static Logger log = Logger.getLogger(WaitAction.class);

    // This class is used to close the web driver which in turn closes the window
    public WaitAction() {
    }

    public void performAction() {
        TestParameter elementToBeClickable = getTestCommand().getParameterByPath("clickable::id");
        TestParameter elementToBePresentId = getTestCommand().getParameterByPath("presence::id");
        TestParameter elementToBePresentXPath = getTestCommand().getParameterByPath("presence::xPath");
        TestParameter javaScriptToBeTrue = getTestCommand().getParameterByPath("frameLoaded::frame");
        TestParameter waitForTime = getTestCommand().getParameterByPath("specificTime");

        try {
            if (elementToBeClickable != null) { // If it is specified, wait until this element is clickable
                WebDriverWait wait = new WebDriverWait(getDriver(), 10);
                wait.until(ExpectedConditions.elementToBeClickable(findElement(elementToBeClickable)));
            }
            if (elementToBePresentId != null) { // If it is specified, wait until this element is present
                WebDriverWait wait = new WebDriverWait(getDriver(), 10);
                wait.until(ExpectedConditions.presenceOfElementLocated(findElement(elementToBePresentId)));
            }
            if (elementToBePresentXPath != null) { // If it is specified, wait until this element is present
                WebDriverWait wait = new WebDriverWait(getDriver(), 10);
                wait.until(ExpectedConditions.presenceOfElementLocated(findElement(elementToBePresentXPath)));
            }
            if (javaScriptToBeTrue != null) { // If it is specified, wait for this javascript to be true
                WebDriverWait wait = new WebDriverWait(getDriver(), 10);
                wait.until((WebDriver driver) -> ((JavascriptExecutor) driver).executeScript("return $('#" + javaScriptToBeTrue.getParameterValue() + "').contents()[0].readyState").equals("complete"));
            }
            if (waitForTime != null) { // If it is specified, wait for this amount of time
                Thread.sleep(Long.parseLong(waitForTime.getParameterValue()));
            }
        } catch (org.openqa.selenium.TimeoutException ex) {
            log.error("Element could not be found within the set time limit of 10 seconds", ex);
        } catch (InterruptedException ex) {
            log.error(ex);
        }
    }
}
