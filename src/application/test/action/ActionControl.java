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

/**
 * This class acts as a controller and template for all other Actions and holds the main information for extended actions.
 */

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

    /**
     *   We need to manually link which written commands link to which classes.
     */
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
        actionClasses.put("run", RunAction.class);
        actionClasses.put("call", CallAction.class);
        actionClasses.put("function", FunctionAction.class);
        actionClasses.put("end", EndAction.class);
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

    /**
     * This method is extended by other action classes and handles the main work of the action including interpreting
     * the {@link application.test.TestParameter} values passed in, performing the action and also constructing the
     * {@link application.test.TestResult} which contains the results of the test.
     */
    public void performAction() {

    }

    /**
     * Takes a screenshot of the current Selenium {@link org.openqa.selenium.WebElement}.  A red box will be drawn
     * around the element to show where it is on the page.
     *
     * @param testStep    The {@link application.test.TestStep} we want to save the screenshot to.
     * @param testElement The {@link org.openqa.selenium.WebElement} we are taking the screenshot of.
     */
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

    /**
     * This methods converts the page source returned by Selenium and converts it into a Jsoup Document.
     * <p>
     * The reason for this is the overhead that is incurred when interacting with Selenium.  A large amount of
     * actions against Selenium can cause the WebDriver to run close and get overloaded.  Previously it was causing
     * issues with the number of ports on the local machine.  Selenium uses local ports to connect to the WebDriver
     * browser and requesting too much too fast exhuasts the current ports that are available.
     * <p>
     * Using Jsoup for all lookups and checking the page speeds up the code and drastically and reduces the amount of work
     * we are doing in Selenium.
     */
    public void refreshCurrentDocument() {
        String pageSource = getDriver().getPageSource();

        // We check to see if the page source is null, if it is we return a default empty page as to not create null pointers
        if (pageSource != null) {
            currentDocument = Jsoup.parse(pageSource);
        } else {
            currentDocument = Jsoup.parse("<html></html>");
        }
    }

    /**
     * This exists for use by actions which extend this class.
     *
     * @return Gets {@link org.jsoup.nodes.Document} that holds the representation for this action.
     */
    public Document getCurrentDocument() {
        return currentDocument;
    }

    /**
     * This exists for use by actions which extend this class.
     *
     * @return Gets {@link org.openqa.selenium.WebDriver} that will be used for this action.
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * @return Gets the current {@link application.net.proxy.snoop.HttpProxyServer} that is handling these actions.
     */
    public HttpProxyServer getHttpProxyServer() {
        return httpProxyServer;
    }

    /**
     * This exists for use by actions which extend this class.
     *
     * @return Gets {@link application.test.TestCommand} for this action.
     */
    public TestCommand getTestCommand() {
        return testCommand;
    }

    /**
     * This exists for use by actions which extend this class.
     *
     * @return Gets {@link application.test.TestResult} for this action.
     */
    public TestResult getTestResult() {
        return testResult;
    }

    /**
     * @return
     */
    public TestNode getParentTestNode() {
        return parentTestNode;
    }

    /**
     * @param actionName
     * @return
     */
    public static Class getClassMapping(String actionName) {
        return actionClasses.get(actionName);
    }

    /**
     * @param driver The {@link org.openqa.selenium.WebDriver} that will be used for this action.
     */
    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * @param testCommand The {@link application.test.TestCommand} that will be used for this action.
     */
    public void setTestCommand(TestCommand testCommand) {
        this.testCommand = testCommand;
    }

    /**
     * @param testResult The {@link application.test.TestResult} that will hold the outcome of this action.
     */
    public void setTestResult(TestResult testResult) {
        this.testResult = testResult;
    }

    /**
     * This function takes a TestParameter object and returns a selenium 'By' object based on how the element is referenced.
     * <p>
     * Both XPath and element Id are handled automatically.
     *
     * @param elementToFind The {@link application.test.TestParameter} that contains the references to the elements and the method that we went to find.
     * @return Returns a Selenium {@link org.openqa.selenium.By} which can be used to find the element.
     */
    public By findElement(TestParameter elementToFind) {
        if ("xPath".equals(elementToFind.getParameterName())) {
            return By.xpath(elementToFind.getParameterValue());
        } else if ("id".equals(elementToFind.getParameterName())) {
            return By.id(elementToFind.getParameterValue());
        }

        return null;
    }
}
