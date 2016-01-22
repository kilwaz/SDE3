package application.test.action;

import application.test.TestParameter;
import application.test.TestStep;
import application.test.action.helpers.LoopedWebElement;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * This action can manipulate a select box within the browser.
 * <p>
 * You can select an option by name or value.
 */
public class SelectWebAction extends WebAction {

    private static Logger log = Logger.getLogger(SelectWebAction.class);

    public SelectWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestStep testStep = TestStep.create(TestStep.class);
        testStep.setParentResult(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter idElement = getTestCommand().getParameterByPath("id");
        TestParameter xPathElement = getTestCommand().getParameterByPath("xPath");
        TestParameter loopElement = getTestCommand().getParameterByName("loop");
        TestParameter selectText = getTestCommand().getParameterByName("select");
        TestParameter selectIndex = getTestCommand().getParameterByName("index");

        By testBy = null;

        WebElement testElement = null;
        if (idElement.exists() || xPathElement.exists()) {
            if (xPathElement.exists()) {
                testBy = findElement(xPathElement);
            } else if (idElement.exists()) {
                testBy = findElement(idElement);
            }

            if (testBy != null) {
                testElement = getDriver().findElement(testBy);
                processElement(testElement, testStep);
            }
        } else if (loopElement.exists()) {
            WebElement loopedElement = null;
            LoopedWebElement loopedWebElement = getLoopTracker().getLoop(loopElement.getParameterValue()).getCurrentLoopWebElement();
            if (loopedWebElement != null) {
                loopedElement = loopedWebElement.getWebElement(getDriver());
            }

            processElement(loopedElement, testStep);
        }

        refreshCurrentDocument();

        if (testElement != null) {
            if (selectIndex.exists()) {
                Select select = new Select(testElement);
                select.selectByIndex(Integer.parseInt(selectIndex.getParameterValue()));
            } else if (selectText.exists()) {
                Boolean selectOptionExists = false;
                Document document = getCurrentDocument();
                Elements options = document.select("select > option");
                for (Element element : options) {
                    if (element.text().equalsIgnoreCase(selectText.getParameterValue())) {
                        selectOptionExists = true;
                        break;
                    }
                }

                if (selectOptionExists) {
                    if (testElement.isDisplayed()) { // WebDriver seems to have issues selecting options when the select box is not visible
                        Select select = new Select(testElement);
                        select.selectByVisibleText(selectText.getParameterValue());
                    }
                } else {
                    log.info("No option exists for " + selectText.getParameterValue() + " to be selected");
                }
            }
        }

        testStep.save();
    }

    private void processElement(WebElement webElement, TestStep testStep) {
        if (webElement != null) {
            takeScreenshotOfElement(testStep, webElement);
            testStep.setTestString(getTestCommand().getRawCommand());
        }
    }
}
