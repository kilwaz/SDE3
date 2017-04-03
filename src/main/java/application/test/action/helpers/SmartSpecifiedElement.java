package application.test.action.helpers;

import application.test.TestParameter;
import application.utils.SDEUtils;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SmartSpecifiedElement {
    private TestParameter idElement;
    private TestParameter nameElement;
    private TestParameter xPathElement;
    private TestParameter loopElement;

    private Element jsoupElement;

    private WebDriver webDriver;
    private LoopTracker loopTracker;
    private DocumentTracker documentTracker;

    public SmartSpecifiedElement(TestParameter idElement, TestParameter nameElement, TestParameter xPathElement, TestParameter loopElement, WebDriver webDriver, LoopTracker loopTracker, DocumentTracker documentTracker) {
        this.idElement = idElement;
        this.nameElement = nameElement;
        this.xPathElement = xPathElement;
        this.loopElement = loopElement;
        this.webDriver = webDriver;
        this.loopTracker = loopTracker;
        this.documentTracker = documentTracker;
    }

    public Element getJSoupElement() {
        Element testElement = null;
        if (idElement.exists()) { // Get the element via id
            testElement = SDEUtils.getJSoupElementFromWebElement(webDriver.findElement(findElement(idElement)), documentTracker.getCurrentDocument());
        } else if (xPathElement.exists()) { // Get the element via xPath
            testElement = SDEUtils.getJSoupElementFromWebElement(webDriver.findElement(findElement(xPathElement)), documentTracker.getCurrentDocument());
        } else if (loopElement.exists()) { // Get element via loop
            if (loopTracker.getLoop(loopElement.getParameterValue()) != null) {
                LoopedWebElement loopedWebElement = (LoopedWebElement) loopTracker.getLoop(loopElement.getParameterValue()).getCurrentLoopObject();
                if (loopedWebElement != null) {
                    testElement = loopedWebElement.getElement();
                }
            }
        } else if (nameElement.exists()) { // Get element via name
            testElement = SDEUtils.getJSoupElementFromWebElement(webDriver.findElement(findElement(nameElement)), documentTracker.getCurrentDocument());
        }

        return testElement;
    }

    public WebElement getSeleniumElement() {
        WebElement testElement = null;
        if (idElement.exists()) { // Get the element via id
            testElement = webDriver.findElement(findElement(idElement));
        } else if (xPathElement.exists()) { // Get the element via xPath
            testElement = webDriver.findElement(findElement(xPathElement));
        } else if (loopElement.exists()) { // Get element via loop
            if (loopTracker.getLoop(loopElement.getParameterValue()) != null) {
                LoopedWebElement loopedWebElement = (LoopedWebElement) loopTracker.getLoop(loopElement.getParameterValue()).getCurrentLoopObject();
                if (loopedWebElement != null) {
                    testElement = loopedWebElement.getWebElement(webDriver);
                }
            }
        } else if (nameElement.exists()) { // Get element via name
            testElement = webDriver.findElement(findElement(nameElement));
        }

        return testElement;
    }

    private By findElement(TestParameter elementToFind) {
        if ("xPath".equals(elementToFind.getParameterName())) {
            return By.xpath(elementToFind.getParameterValue());
        } else if ("id".equals(elementToFind.getParameterName())) {
            return By.id(elementToFind.getParameterValue());
        } else if ("name".equals(elementToFind.getParameterName())) {
            return By.name(elementToFind.getParameterValue());
        }

        return null;
    }
}
