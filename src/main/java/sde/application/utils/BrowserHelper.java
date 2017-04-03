package sde.application.utils;

import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import sde.application.utils.managers.BrowserManager;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class BrowserHelper {
    private static final String CHROME_DRIVER = "chromedriver-2.28.exe";
    private static final String IE_DRIVER = "IEDriverServer-2.53.1.exe";
    private static final String OPREA_DRIVER = "operadriver-0.2.2.exe";

    private static Logger log = Logger.getLogger(BrowserHelper.class);

    public static WebDriver getChrome() {
        return getChrome("localhost:8080");
    }

    public static WebDriver getFireFox() {
        return getFireFox("localhost:8080");
    }

    public static WebDriver getOpera() {
        return getOpera("localhost:8080");
    }

    public static WebDriver getIE() {
        return getIE("localhost:8080");
    }

    public static WebDriver getChrome(String proxyConnectionString) {
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();

        if (SDEUtils.isJar()) {
            try {
                URI uri = SDEUtils.getFile(SDEUtils.getJarURI(), "WebDrivers/" + CHROME_DRIVER);
                System.setProperty("webdriver.chrome.driver", uri.getPath());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            System.setProperty("webdriver.chrome.driver", SDEUtils.getResourcePath() + "/WebDrivers/" + CHROME_DRIVER);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        Proxy proxy = new Proxy();
        proxy.setHttpProxy(proxyConnectionString)
                .setFtpProxy(proxyConnectionString)
                .setSslProxy(proxyConnectionString)
                .setSocksProxy(proxyConnectionString);
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

    public static WebDriver getFireFox(String proxyConnectionString) {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();

        Proxy proxy = new Proxy();
        proxy.setHttpProxy(proxyConnectionString)
                .setFtpProxy(proxyConnectionString)
                .setSslProxy(proxyConnectionString);
        capabilities.setCapability(CapabilityType.PROXY, proxy);

        WebDriver driver = new FirefoxDriver(capabilities);
        setupBrowser(driver);
        return driver;
    }

    public static WebDriver getOpera(String proxyConnectionString) {
        DesiredCapabilities capabilities = DesiredCapabilities.operaBlink();

        if (SDEUtils.isJar()) {
            try {
                URI uri = SDEUtils.getFile(SDEUtils.getJarURI(), "WebDrivers/" + OPREA_DRIVER);
                System.setProperty("webdriver.opera.driver", uri.getPath());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            System.setProperty("webdriver.opera.driver", SDEUtils.getResourcePath() + "/WebDrivers/" + OPREA_DRIVER);
        }

        OperaOptions options = new OperaOptions();
        options.addArguments("ignore-certificate-errors");
        capabilities.setCapability(OperaOptions.CAPABILITY, options);

        Proxy proxy = new Proxy();
        proxy.setHttpProxy(proxyConnectionString)
                .setFtpProxy(proxyConnectionString)
                .setSslProxy(proxyConnectionString);
        capabilities.setCapability(CapabilityType.PROXY, proxy);

        WebDriver driver = new OperaDriver(capabilities);
        setupBrowser(driver);
        return driver;
    }

    public static WebDriver getIE(String proxyConnectionString) {
        DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();

        if (SDEUtils.isJar()) {
            try {
                URI uri = SDEUtils.getFile(SDEUtils.getJarURI(), "WebDrivers/" + IE_DRIVER);
                System.setProperty("webdriver.ie.driver", uri.getPath());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            System.setProperty("webdriver.ie.driver", SDEUtils.getResourcePath() + "/WebDrivers/" + IE_DRIVER);
        }

        Proxy proxy = new Proxy();
        proxy.setHttpProxy(proxyConnectionString)
                .setFtpProxy(proxyConnectionString)
                .setSslProxy(proxyConnectionString);
        capabilities.setCapability(CapabilityType.PROXY, proxy);
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

        WebDriver driver = new InternetExplorerDriver(capabilities);
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
