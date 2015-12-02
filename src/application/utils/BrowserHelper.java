package application.utils;

import application.utils.managers.BrowserManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

public class BrowserHelper {
    private static Logger log = Logger.getLogger(BrowserHelper.class);

    public static WebDriver getChrome() {
        return getChrome("localhost:8080");
    }

    public static WebDriver getChrome(String proxyConnectionString) {
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        Proxy proxy = new Proxy();
        proxy.setHttpProxy(proxyConnectionString)
                .setFtpProxy(proxyConnectionString)
                .setSslProxy(proxyConnectionString);
        capabilities.setCapability(CapabilityType.PROXY, proxy);

        WebDriver driver = new ChromeDriver(capabilities);
        setupBrowser(driver);
        return driver;
    }

    public static WebDriver getRemoteChrome(String proxyConnectionString, String seleniumGridUrl) {
        try {
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("test-type");
            capabilities.setCapability(ChromeOptions.CAPABILITY, options);

            Proxy proxy = new Proxy();
            proxy.setHttpProxy(proxyConnectionString)
                    .setFtpProxy(proxyConnectionString)
                    .setSslProxy(proxyConnectionString);
            capabilities.setCapability(CapabilityType.PROXY, proxy);

            WebDriver driver = new RemoteWebDriver(new URL(seleniumGridUrl), capabilities);
            setupBrowser(driver);

            BrowserManager.getInstance().addBrowser(driver);
            return driver;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
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

    private static void setupBrowser(WebDriver driver) {
        Double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        Double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        driver.manage().window().setSize(new Dimension(screenWidth.intValue(), screenHeight.intValue()));
        driver.manage().window().setPosition(new Point(0, 0));
        BrowserManager.getInstance().addBrowser(driver);

        driver.manage().deleteAllCookies();
    }
}
