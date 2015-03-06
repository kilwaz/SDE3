package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class TestAction extends ActionControl {
    // This class is used to close the web driver which in turn closes the window
    public TestAction() {
    }

    public void performAction() {
        TestParameter xPathElement = getTestCommand().getParameterByPath("element::xPath");
        By testBy = null;

        if (xPathElement != null) {
            testBy = findElement(xPathElement);
        }

        if (testBy != null) {
            WebElement testElement = getDriver().findElement(testBy);

            if (getTestCommand().getParameterByPath("equals") != null) {
                TestStep testStep = DataBank.createNewTestStep(getTestResult());
                getTestResult().addTestStep(testStep);
                testStep.setTestString(getTestCommand().getRawCommand());

                String contentToEqual = getTestCommand().getParameterByPath("equals").getParameterValue();
                String content = testElement.getText().trim();

                takeScreenshotOfElement(testStep, testElement);

                if (contentToEqual.equals(content)) {
                    System.out.println("SAME - EXPECTED " + contentToEqual + " GOT " + content);
                    testStep.setSuccessful(true);
                } else {
                    System.out.println("DIFFERENT - EXPECTED " + contentToEqual + " GOT " + content);
                    testStep.setSuccessful(false);
                }
                DataBank.saveTestStep(testStep);
            }
        }
    }
}
