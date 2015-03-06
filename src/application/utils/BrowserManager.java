package application.utils;

import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

public class BrowserManager {
    private static BrowserManager browserManager;
    private List<WebDriver> runningBrowsers;

    public BrowserManager() {
        browserManager = this;
        runningBrowsers = new ArrayList<>();
    }

    public void addBrowser(WebDriver driver) {
        runningBrowsers.add(driver);
    }

    public void closeBrowsers() {
        runningBrowsers.forEach(org.openqa.selenium.WebDriver::quit);
    }

    public static BrowserManager getInstance() {
        return browserManager;
    }
}
