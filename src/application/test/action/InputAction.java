package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class InputAction extends ActionControl {
    private static Logger log = Logger.getLogger(InputAction.class);

    // This class is used to input a value into an element
    public InputAction() {
    }

    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter elementId = getTestCommand().getParameterByPath("id");
        TestParameter valueToEnter = getTestCommand().getParameterByPath("value");
        TestParameter characterDelay = getTestCommand().getParameterByPath("characterDelay");
        if (elementId != null && valueToEnter != null) {
            WebElement testElement = getDriver().findElement(By.id(elementId.getParameterValue()));
            if (testElement != null) {

                // This delays the input of the text to simulate as if the user were typing it themselves
                if (characterDelay != null) {
                    Long delay = Long.parseLong(characterDelay.getParameterValue());
                    char[] textSplit = valueToEnter.getParameterValue().toCharArray();

                    for (Character c : textSplit) {
                        testElement.sendKeys(c.toString());

                        // Pause the current thread for the time that we need
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    testElement.sendKeys(valueToEnter.getParameterValue());
                }
                takeScreenshotOfElement(testStep, testElement);
                testStep.setTestString(getTestCommand().getRawCommand());
            }
        }
        DataBank.saveTestStep(testStep);
    }
}
