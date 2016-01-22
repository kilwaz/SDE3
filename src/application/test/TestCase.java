package application.test;

import application.test.action.helpers.PageStateCapture;
import application.utils.Timer;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestCase {
    private String elementType;
    private String elementId;
    private String elementFrame;
    private String inputValue;
    private String expectedOutputValue;
    private String involvedFrames;

    public TestCase() {

    }

    public String getElementId() {
        return this.elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getInputValue() {
        return this.inputValue;
    }

    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }

    public String getExpectedOutputValue() {
        return this.expectedOutputValue;
    }

    public void setExpectedOutputValue(String expectedOutputValue) {
        this.expectedOutputValue = expectedOutputValue;
    }

    public String getElementFrame() {
        return this.elementFrame;
    }

    public void setElementFrame(String elementFrame) {
        this.elementFrame = elementFrame;
    }

    public String getElementType() {
        return this.elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public TestResultOld evaluate(WebDriver driver) {
        PageStateCapture initialState = new PageStateCapture(elementFrame, "Case");
        initialState.capturePage(driver);

        TestResultOld testResultOld = new TestResultOld();
        testResultOld.setInitialState(initialState);

        if (!"default".equals(elementFrame)) {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id(elementFrame)));
            driver.switchTo().frame(elementFrame);
        }

        if ("select".equals(elementType)) {
            Select select = new Select(driver.findElement(By.id(elementId)));
            select.selectByVisibleText(inputValue);

            testResultOld.setExpected(expectedOutputValue);
            Timer timer = new Timer();
            testResultOld.setOutcome(testSelectCase(driver, select.getFirstSelectedOption().getAttribute("value")));
            testResultOld.setDuration(timer.getDuration());
        } else if ("link".equals(elementType)) {
            testResultOld.setExpected(expectedOutputValue);
            Timer timer = new Timer();
            testResultOld.setOutcome(testLinkCase(driver));
            testResultOld.setDuration(timer.getDuration());
        }

        if (!"default".equals(elementFrame)) {
            driver.switchTo().defaultContent();
        }

        PageStateCapture finalState = new PageStateCapture(elementFrame, "final");
        testResultOld.setFinalState(finalState);

        finalState.capturePage(driver);
        testResultOld.setChangedElements(initialState.compare(finalState));

        return testResultOld;
    }

    private String testLinkCase(WebDriver driver) {
        WebElement linkElement = driver.findElement(By.xpath(elementId));

        if (linkElement != null) {
            linkElement.click();
            return "SUCCESS";
        }

        return "FAIL";
    }

    private String testSelectCase(WebDriver driver, String value) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(elementId)));

        Select select = new Select(driver.findElement(By.id(elementId)));
        try {
            select.selectByValue(value);
        } catch (NoSuchElementException ex) {
            return "FAIL Cannot find " + value + " within select box";
        }

        return wait.until(new Function<WebDriver, String>() {
            public String apply(WebDriver driver) {
                Select ajaxSelect = new Select(driver.findElement(By.id(elementId)));
                WebElement ajaxElement = driver.findElement(By.id(elementId));
                String className = ajaxElement.getAttribute("className");

                if (className.contains("inputChangeComplete")) {
                    return ajaxSelect.getFirstSelectedOption().getText().trim();
                } else {
                    return null;
                }
            }
        });
    }
}
