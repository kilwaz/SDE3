package application.utils.managers;

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
        runningBrowsers.clear();
    }

    public static BrowserManager getInstance() {
        if (browserManager == null) {
            return new BrowserManager();
        } else {
            return browserManager;
        }
    }
}
