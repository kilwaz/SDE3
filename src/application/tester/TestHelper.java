package application.tester;

import application.utils.DataBank;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestHelper {
    public static TestCase createTestCase(String elementId, String elementFrame, String inputValue, String expectedOutputValue, String testType) {
        TestCase testCase = new TestCase();

        testCase.setElementType(testType);
        testCase.setElementFrame(elementFrame);
        testCase.setElementId(elementId);
        testCase.setExpectedOutputValue(expectedOutputValue);
        testCase.setInputValue(inputValue);

        return testCase;
    }

    public static List<String> findEmployeeIDsOnWorksheet(WebDriver driver) {
        List<String> employeeID = new ArrayList<String>();

        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("content")));
        driver.switchTo().frame("content");

        WebElement scroll = driver.findElement(By.id("scroll"));
        List<WebElement> rows = scroll.findElements(By.tagName("tr"));

        for (WebElement row : rows) {
            WebElement comment = row.findElement(By.className("comment"));
            String[] split = comment.getAttribute("id").split("\\.");
            employeeID.add(split[1]);
        }

        driver.switchTo().defaultContent();

        return employeeID;
    }

    public static List<String> getWorksheetEmployeeRow(WebDriver driver) {
        List<String> innerHTML = new ArrayList<String>();

        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("content")));
        driver.switchTo().frame("content");

        WebElement scroll = driver.findElement(By.id("row0"));
        List<WebElement> columns = scroll.findElements(By.tagName("td"));

        for (WebElement column : columns) {
            innerHTML.add(column.getAttribute("outerHTML").trim());
        }

        driver.switchTo().defaultContent();

        return innerHTML;
    }

    public static String getContentSource(WebDriver driver, String frame) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(frame)));
        driver.switchTo().frame(frame);

        String source = driver.getPageSource();

        driver.switchTo().defaultContent();
        return source;
    }

    public static void wait(WebDriver driver, int milliseconds) {
        driver.manage().timeouts().implicitlyWait(milliseconds, TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static TestResultNode getResultSet(String resultSet, String referenceID) {
        return (TestResultNode) DataBank.getInstanceObject(referenceID, resultSet);
    }
}
