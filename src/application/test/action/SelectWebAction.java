package application.test.action;

import application.test.TestParameter;
import application.test.action.helpers.LoopedWebElement;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.concurrent.TimeUnit;

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
        try {
            TestParameter idElement = getTestCommand().getParameterByPath("id");
            TestParameter xPathElement = getTestCommand().getParameterByPath("xPath");
            TestParameter loopElement = getTestCommand().getParameterByName("loop");
            TestParameter selectText = getTestCommand().getParameterByName("select");
            TestParameter selectIndex = getTestCommand().getParameterByName("index");

            // We only wait for 10 seconds for page loads, sometimes the click hangs forever otherwise
            getDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

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
                    processElement(testElement);
                }
            } else if (loopElement.exists()) {
                WebElement loopedElement = null;
                LoopedWebElement loopedWebElement = (LoopedWebElement) getLoopTracker().getLoop(loopElement.getParameterValue()).getCurrentLoopObject();
                if (loopedWebElement != null) {
                    loopedElement = loopedWebElement.getWebElement(getDriver());
                }

                processElement(loopedElement);
            }

            getDocumentTracker().refreshCurrentDocument();

            if (testElement != null) {
                if (selectIndex.exists()) {
                    Select select = new Select(testElement);
                    select.selectByIndex(Integer.parseInt(selectIndex.getParameterValue()));
                } else if (selectText.exists()) {
                    Boolean selectOptionExists = false;
                    Document document = getDocumentTracker().getCurrentDocument();
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
                        if (getRunningTest() != null && getRunningTest().getTestCase() != null) {
                            getRunningTest().getTestCase().log("No option exists for " + selectText.getParameterValue() + " to be selected");
                        }
                    }
                }
            }

        } catch (Exception ex) {
            application.error.Error.SELENIUM_SELECT_ACTION_NOT_FOUND.record().create(ex);
        } finally {
            // We sent the driver back to being unlimited timeout for page loads
            getDriver().manage().timeouts().pageLoadTimeout(-1, TimeUnit.SECONDS);
        }
    }

    private void processElement(WebElement webElement) {
        if (webElement != null) {
            takeScreenshotOfElement(webElement);
        }
    }
}
