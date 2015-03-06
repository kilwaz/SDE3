package application.test.action;

import application.test.TestParameter;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WaitAction extends ActionControl {
    // This class is used to close the web driver which in turn closes the window
    public WaitAction() {
    }

    public void performAction() {
        TestParameter elementToBeClickable = getTestCommand().getParameterByPath("clickable::id");
        TestParameter elementToBePresent = getTestCommand().getParameterByPath("presence::id");
        try {
            if (elementToBeClickable != null) { // If it is specified wait until this element is clickable
                WebDriverWait wait = new WebDriverWait(getDriver(), 10);
                wait.until(ExpectedConditions.elementToBeClickable(By.id(elementToBeClickable.getParameterValue())));
            }
            if (elementToBePresent != null) { // If it is specified wait until this element is clickable
                WebDriverWait wait = new WebDriverWait(getDriver(), 10);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id(elementToBePresent.getParameterValue())));
            }
        } catch (org.openqa.selenium.TimeoutException ex) {
            System.out.println("Element could not be found within the set time limit");
            ex.printStackTrace();
        }
    }
}
