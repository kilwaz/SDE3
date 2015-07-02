package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ClickAction extends ActionControl {

    // This class is used to click on an element
    public ClickAction() {
    }

    public void performAction() {
        try {
            TestStep testStep = DataBank.createNewTestStep(getTestResult());
            getTestResult().addTestStep(testStep);

            TestParameter xPathElement = getTestCommand().getParameterByPath("xPath");
            TestParameter idElement = getTestCommand().getParameterByPath("id");
            By testBy = null;

            if (xPathElement != null) {
                testBy = findElement(xPathElement);
            } else if (idElement != null) {
                testBy = findElement(idElement);
            }

            if (testBy != null) {
                WebElement testElement = getDriver().findElement(testBy);
                if (testElement != null) {
                    takeScreenshotOfElement(testStep, testElement);
                    testStep.setTestString(getTestCommand().getRawCommand());
                    testElement.click();
                }
            }

            DataBank.saveTestStep(testStep);
        } catch (Exception ex) {
            System.out.println("Within click failing");
            ex.printStackTrace();
        }
    }
}
