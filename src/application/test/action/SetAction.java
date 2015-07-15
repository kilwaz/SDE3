package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class SetAction extends ActionControl {

    private static Logger log = Logger.getLogger(LogAction.class);

    // This class is used to input a value into an element
    public SetAction() {
    }

    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter idElement = getTestCommand().getParameterByName("id");

        DataBank.saveTestStep(testStep);
    }
}
