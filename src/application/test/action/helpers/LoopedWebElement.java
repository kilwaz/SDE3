package application.test.action.helpers;

import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoopedWebElement extends LoopedObject {
    private Element element = null;
    private String xPath = "";

    private static Logger log = Logger.getLogger(LoopedWebElement.class);

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
            webElement = webDriver.findElement(By.xpath(xPath));
        } catch (Exception ex) {
            log.info("Selenium could not find the element with xPath " + xPath, ex);
        }

        return webElement;
    }

    public String getXPath() {
        return xPath;
    }
}
