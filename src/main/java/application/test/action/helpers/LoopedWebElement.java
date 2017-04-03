package application.test.action.helpers;

import application.error.Error;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.openqa.selenium.*;

public class LoopedWebElement extends LoopedObject {
    private static Logger log = Logger.getLogger(LoopedWebElement.class);
    private Element element = null;
    private String xPath = "";

    public LoopedWebElement(Element element) {
        this.element = element;
        xPath = SDEUtils.generateXPath(element);
    }

    public Element getElement() {
        return element;
    }

    public WebElement getWebElement(WebDriver webDriver) {
        // Here we get the element from Selenium as it is ready to be used
        WebElement webElement = null;
        try {
            try { // We need to check this first as trying to find an element with xpath if an alert is open causes selenium to hang
                Alert alert = webDriver.switchTo().alert();
                log.info("Auto closing alert popup");
                alert.accept();
            } catch (NoAlertPresentException ex) {
                // We don't really want to do anything with this exception, if the alert doesn't exist then great
            }
            webElement = webDriver.findElement(By.xpath(xPath));
        } catch (Exception ex) {
            Error.SELENIUM_XPATH_NOT_FOUND.record().additionalInformation("Xpath = " + xPath).create(ex);
        }

        return webElement;
    }

    public String getXPath() {
        return xPath;
    }
}
