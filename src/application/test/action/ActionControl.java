package application.test.action;

import application.test.TestCommand;
import application.test.TestParameter;
import application.test.TestResult;
import application.test.TestStep;
import application.net.proxy.WebProxy;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ActionControl {
    private WebDriver driver = null;
    private TestCommand testCommand = null;
    private TestResult testResult = null;
    private WebProxy webProxy = null;

    // This is used as a reference to match up action names used within the TestNode to the class name which will handle the action
    private static HashMap<String, Class> actionClasses = new HashMap<>();

    static {
        actionClasses.put("input", InputAction.class);
        actionClasses.put("click", ClickAction.class);
        actionClasses.put("exit", ExitAction.class);
        actionClasses.put("url", URLAction.class);
        actionClasses.put("frame", FrameAction.class);
        actionClasses.put("test", TestAction.class);
        actionClasses.put("wait", WaitAction.class);
        actionClasses.put("track", TrackAction.class);
    }

    public ActionControl() {
    }

    public void initialise(WebProxy webProxy, WebDriver driver, TestCommand testCommand, TestResult testResult) {
        this.webProxy = webProxy;
        this.driver = driver;
        this.testCommand = testCommand;
        this.testResult = testResult;
    }

    public void performAction() {
        // This is overridden by the extended action classes
    }

    public void takeScreenshotOfElement(TestStep testStep, WebElement testElement) {
        Dimension elementDimension = testElement.getSize();
        Point elementLocation = testElement.getLocation();

        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        try {
            BufferedImage bufferedImage = ImageIO.read(scrFile);
            Graphics2D g = bufferedImage.createGraphics();
            g.setColor(java.awt.Color.RED);
            g.drawRect(elementLocation.getX(), elementLocation.getY(), elementDimension.getWidth(), elementDimension.getHeight());

            testStep.setScreenshot(bufferedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    public WebProxy getWebProxy() {
        return webProxy;
    }

    public TestCommand getTestCommand() {
        return testCommand;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    public static Class getClassMapping(String actionName) {
        return actionClasses.get(actionName);
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public void setTestCommand(TestCommand testCommand) {
        this.testCommand = testCommand;
    }

    public void setTestResult(TestResult testResult) {
        this.testResult = testResult;
    }

    // This function takes a TestParameter object and returns a selenium 'By' object based on how the element is referenced
    public By findElement(TestParameter elementToFind) {
        if ("xPath".equals(elementToFind.getParameterName())) {
            return By.xpath(elementToFind.getParameterValue());
        } else if ("id".equals(elementToFind.getParameterName())) {
            return By.id(elementToFind.getParameterValue());
        }

        return null;
    }
}
