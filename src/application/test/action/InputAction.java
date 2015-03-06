package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class InputAction extends ActionControl {

    // This class is used to input a value into an element
    public InputAction() {
    }

    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter elementId = getTestCommand().getParameterByName("id");
        TestParameter valueToEnter = getTestCommand().getParameterByPath("value");
        if (elementId != null && valueToEnter != null) {
            WebElement testElement = getDriver().findElement(By.id(elementId.getParameterValue()));
            if (testElement != null) {
                testElement.sendKeys(valueToEnter.getParameterValue());
                takeScreenshotOfElement(testStep, testElement);
                testStep.setTestString(getTestCommand().getRawCommand());
            }
        }
        DataBank.saveTestStep(testStep);
    }
}
