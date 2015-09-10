package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import application.test.action.helpers.LoopTracker;
import application.test.action.helpers.LoopedWebElement;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class SelectAction extends ActionControl {

    private static Logger log = Logger.getLogger(SelectAction.class);

    public SelectAction() {
    }

    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter idElement = getTestCommand().getParameterByPath("id");
        TestParameter xPathElement = getTestCommand().getParameterByPath("xPath");
        TestParameter loopElement = getTestCommand().getParameterByName("loop");
        TestParameter selectText = getTestCommand().getParameterByName("select");

        By testBy = null;

        WebElement testElement = null;
        if (idElement != null || xPathElement != null) {
            if (xPathElement != null) {
                testBy = findElement(xPathElement);
            } else if (idElement != null) {
                testBy = findElement(idElement);
            }

            if (testBy != null) {
                testElement = getDriver().findElement(testBy);
                processElement(testElement, testStep);
            }
        } else if (loopElement != null) {
            WebElement loopedElement = null;
            LoopedWebElement loopedWebElement = LoopTracker.getLoop(loopElement.getParameterValue()).getCurrentLoopWebElement();
            if (loopedWebElement != null) {
                loopedElement = loopedWebElement.getWebElement(getDriver());
            }

            processElement(loopedElement, testStep);
        }

        if (testElement != null) {
            Select select = new Select(testElement);
            select.selectByVisibleText(selectText.getParameterValue());
        }
        DataBank.saveTestStep(testStep);
    }

    private void processElement(WebElement webElement, TestStep testStep) {
        if (webElement != null) {
            takeScreenshotOfElement(testStep, webElement);
            testStep.setTestString(getTestCommand().getRawCommand());
            webElement.click();
        }
    }
}
