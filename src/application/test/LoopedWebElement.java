package application.test;

import application.utils.SDEUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoopedWebElement {
    private WebElement webElement = null;
    private String xPath = "";

    public LoopedWebElement(WebElement webElement) {
        this.webElement = webElement;
        xPath = SDEUtils.generateXPath(webElement);
    }

    private Boolean isStale() {
        try {
            // Calling any method forces a staleness check
            webElement.isEnabled();
            return false;
        } catch (StaleElementReferenceException expected) {
            return true;
        }
    }

    public WebElement getWebElement(WebDriver webDriver) {
        // Here we check to see if the element is stale, if it is then we try to get it with the xPath we generated on object creation
        if (isStale()) {
            return webDriver.findElement(By.xpath(xPath));
        }

        return webElement;
    }

    public String getXPath() {
        return xPath;
    }
}
