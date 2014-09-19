package application.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;

public class BrowserHelper {
    public static WebDriver getChrome() {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        WebDriver driver = new ChromeDriver(capabilities);
        setupBrowser(driver);
        return driver;
    }

    public static WebDriver getFirefox() {
        WebDriver driver = new FirefoxDriver();
        setupBrowser(driver);
        return driver;
    }

    public static WebDriver getIE() {
        WebDriver driver = new InternetExplorerDriver();
        setupBrowser(driver);
        return driver;
    }

    static void setupBrowser(WebDriver driver) {
        Double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        Double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        driver.manage().window().setSize(new Dimension(screenWidth.intValue(), screenHeight.intValue()));
        driver.manage().window().setPosition(new Point(0, 0));
        BrowserManager.getInstance().addBrowser(driver);
    }

    public static void login(String username, String password, WebDriver driver) {
        WebElement usernameElement = driver.findElement(By.id("j_username"));
        WebElement passwordElement = driver.findElement(By.id("j_password"));
        usernameElement.sendKeys(username);
        passwordElement.sendKeys(password);
        passwordElement.submit();

        // Waits for the hierarchy window to load on the dashboard as this is generally the last thing to load
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("content")));
        driver.switchTo().frame("content");
        wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("hierarchySelectIframe")));
        driver.switchTo().defaultContent();
    }

    public static void gotoWorksheet(WebDriver driver) {
        WebElement worksheetLink = driver.findElement(By.id("Worksheet"));
        worksheetLink.click();

        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("worksheetDetails")));
        driver.switchTo().frame("worksheetDetails");
        wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("hierarchyPopup-text")));
        driver.switchTo().defaultContent();
    }

    public static void gotoLoginPage(String url, WebDriver driver) {
        driver.get(url);

        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login")));
    }

    public static void logout(WebDriver driver) {
        WebElement logout = driver.findElement(By.id("logOut"));
        logout.submit();
    }
}
