package application.test.action;

import application.net.proxy.snoop.HttpProxyServer;
import application.node.implementations.TestNode;
import application.test.TestCommand;
import application.test.TestParameter;
import application.test.TestResult;
import application.test.TestStep;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.HashMap;

public class ActionControl {
    private WebDriver driver = null;
    private TestCommand testCommand = null;
    private TestResult testResult = null;
    private HttpProxyServer httpProxyServer = null;
    private TestNode parentTestNode = null;
    private static Document currentDocument = null;

    private static Logger log = Logger.getLogger(ActionControl.class);

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
        actionClasses.put("log", LogAction.class);
        actionClasses.put("set", SetAction.class);
        actionClasses.put("loop", LoopAction.class);
        actionClasses.put("if", IfAction.class);
    }

    public ActionControl() {
    }

    public void initialise(HttpProxyServer webProxy, WebDriver driver, TestCommand testCommand, TestResult testResult, TestNode parentTestNode) {
        this.parentTestNode = parentTestNode;
        this.httpProxyServer = webProxy;
        this.driver = driver;
        this.testCommand = testCommand;
        this.testResult = testResult;
    }

    public void performAction() {
        // This is overridden by the extended action classes
    }

    public void takeScreenshotOfElement(TestStep testStep, WebElement testElement) {
//        Dimension elementDimension = testElement.getSize();
//        Point elementLocation = testElement.getLocation();
//
//        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
//
//        try {
//            BufferedImage bufferedImage = ImageIO.read(scrFile);
//            Graphics2D g = bufferedImage.createGraphics();
//            g.setColor(java.awt.Color.RED);
//            g.drawRect(elementLocation.getX(), elementLocation.getY(), elementDimension.getWidth(), elementDimension.getHeight());
//
//            testStep.setScreenshot(bufferedImage);
//        } catch (IOException ex) {
//            log.error(ex);
//        }
    }

    public void refreshCurrentDocument() {
        currentDocument = Jsoup.parse(getDriver().getPageSource());
        //log.info("Updated current doc to " + currentDocument.html());
    }

    public Document getCurrentDocument() {
        return currentDocument;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public HttpProxyServer getHttpProxyServer() {
        return httpProxyServer;
    }

    public TestCommand getTestCommand() {
        return testCommand;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    public TestNode getParentTestNode() {
        return parentTestNode;
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
