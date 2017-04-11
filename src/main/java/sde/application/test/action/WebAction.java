package sde.application.test.action;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import sde.application.error.Error;
import sde.application.gui.Program;
import sde.application.net.proxy.snoop.HttpProxyServer;
import sde.application.node.objects.Test;
import sde.application.test.TestCommand;
import sde.application.test.TestCommandScreenshot;
import sde.application.test.TestParameter;
import sde.application.test.TestRunner;
import sde.application.test.action.helpers.*;
import sde.application.utils.AppParams;
import sde.application.utils.SDEUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * This class acts as a controller and template for all other Actions and holds the main information for extended actions.
 */

public abstract class WebAction implements Action {
    private static Logger log = Logger.getLogger(WebAction.class);
    // This is used as a reference to match up action names used within the TestNodes to the class name which will handle the action
    private static HashMap<String, Class> actionClasses = new HashMap<>();

    /*
        We need to manually link which written commands link to which classes.
     */
    static {
        actionClasses.put("input", InputWebAction.class);
        actionClasses.put("click", ClickWebAction.class);
        actionClasses.put("exit", ExitWebAction.class);
        actionClasses.put("url", URLWebAction.class);
        actionClasses.put("frame", FrameWebAction.class);
        actionClasses.put("wait", WaitWebAction.class);
        actionClasses.put("track", TrackWebAction.class);
        actionClasses.put("log", LogWebAction.class);
        actionClasses.put("set", SetWebAction.class);
        actionClasses.put("loop", LoopWebAction.class);
        actionClasses.put("if", IfWebAction.class);
        actionClasses.put("run", RunWebAction.class);
        actionClasses.put("call", CallWebAction.class);
        actionClasses.put("function", FunctionWebAction.class);
        actionClasses.put("end", EndWebAction.class);
        actionClasses.put("select", SelectWebAction.class);
        actionClasses.put("javascript", JavascriptWebAction.class);
        actionClasses.put("driver", DriverWebAction.class);
        actionClasses.put("state", StateWebAction.class);
        actionClasses.put("window", WindowWebAction.class);
        actionClasses.put("keyboard", KeyboardWebAction.class);
        actionClasses.put("screenshot", ScreenshotWebAction.class);
        actionClasses.put("refresh", RefreshWebAction.class);
        actionClasses.put("test", TestWebAction.class);
    }

    private WebDriver driver = null;
    private TestCommand testCommand = null;
    private HttpProxyServer httpProxyServer = null;
    private Program program = null;
    private Test runningTest = null;
    private IfTracker ifTracker = null;
    private LoopTracker loopTracker = null;
    private VariableTracker variableTracker = null;
    private FunctionTracker functionTracker = null;
    private StateTracker stateTracker = null;
    private DocumentTracker documentTracker = null;
    private TestRunner testRunner = null;

    public WebAction() {
    }

    /**
     * Gets the class mapping of action name to Class which manages that action.
     *
     * @param actionName Name of the action in the script being called.
     * @return Class object of the class which handles that action.
     */
    public static Class getClassMapping(String actionName) {
        return actionClasses.get(actionName);
    }

    /**
     * Sets up the action with all the current environment variables needed to run it.
     *
     * @param webProxy    The proxy that will handle the request.
     * @param driver      The Selenium web driver that is handling the test.
     * @param testCommand The full test command to process.
     * @param program     Reference to the program this test is a spawn of.
     */
    public void initialise(HttpProxyServer webProxy, WebDriver driver, TestCommand testCommand, Program program, Test runningTest, IfTracker ifTracker, FunctionTracker functionTracker, LoopTracker loopTracker, VariableTracker variableTracker, StateTracker stateTracker, DocumentTracker documentTracker, TestRunner testRunner) {
        this.program = program;
        this.httpProxyServer = webProxy;
        this.driver = driver;
        this.testCommand = testCommand;
        this.runningTest = runningTest;
        this.ifTracker = ifTracker;
        this.functionTracker = functionTracker;
        this.stateTracker = stateTracker;
        this.loopTracker = loopTracker;
        this.variableTracker = variableTracker;
        this.documentTracker = documentTracker;
        this.testRunner = testRunner;
    }

    public void takeScreenshotOfPage() {
        takeScreenshotOfElement(null);
    }

    /**
     * Takes a screenshot of the current Selenium {@link org.openqa.selenium.WebElement}.  A red box will be drawn
     * around the element to show where it is on the page.
     *
     * @param testElement The {@link org.openqa.selenium.WebElement} we are taking the screenshot of.
     */
    public void takeScreenshotOfElement(WebElement testElement) {
        if (AppParams.getRecordScreenshots()) {
            String elementXPath, scriptToRun, highlightId = "";
            if (testElement != null) {
                elementXPath = SDEUtils.generateXPath(testElement).replace("\"", "'");
                highlightId = "selenium-highlight@" + testCommand.getUuidString();
                scriptToRun = "var elem = document.evaluate(\"" + elementXPath + "\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;\n" +
                        "if(elem != null){\n" +
                        "var box = elem.getBoundingClientRect();\n" +
                        "var div = document.createElement('div');\n" +
                        "div.id = '" + highlightId + "';\n" +
                        "div.style.position = 'absolute';\n" +
                        "div.style.top = (box.top - 4) + \"px\";\n" +
                        "div.style.left = (box.left - 4) + \"px\";\n" +
                        "div.style.width = elem.offsetWidth + \"px\";\n" +
                        "div.style.height = elem.offsetHeight + \"px\";\n" +
                        "div.style.zIndex = 99999;\n" +
                        "div.style.border = \"4px solid #FF0000\";\n" +
                        "document.getElementsByTagName('body')[0].appendChild(div);" +
                        "}";
                try {
                    ((JavascriptExecutor) getDriver()).executeScript(scriptToRun);
                } catch (WebDriverException ex) {
                    Error.SELENIUM_JAVASCRIPT_FAILED.record().additionalInformation("Script:- " + scriptToRun).create(ex);
                }
            }

            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            try {
                BufferedImage bufferedImage = ImageIO.read(scrFile);
                TestCommandScreenshot testCommandScreenshot = TestCommandScreenshot.create(TestCommandScreenshot.class);
                testCommandScreenshot.setParentTestCommand(testCommand);
                testCommandScreenshot.setScreenshot(bufferedImage);
                testCommand.setTestCommandScreenshot(testCommandScreenshot);
                if (testElement != null) {
                    // Remove the highlighting
                    scriptToRun = "var elem = document.getElementById(\"" + highlightId + "\");\n" +
                            "if(elem != null){\n" +
                            "elem.parentNode.removeChild(elem);" +
                            "}";
                    try {
                        ((JavascriptExecutor) getDriver()).executeScript(scriptToRun);
                    } catch (WebDriverException ex) {
                        Error.SELENIUM_JAVASCRIPT_FAILED.record().additionalInformation("Script:- " + scriptToRun).create(ex);
                    }
                }
            } catch (IOException ex) {
                log.error(ex);
            }
        }
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
     * @param driver The {@link org.openqa.selenium.WebDriver} that will be used for this action.
     */
    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * @return Gets the current {@link HttpProxyServer} that is handling these actions.
     */
    public HttpProxyServer getHttpProxyServer() {
        return httpProxyServer;
    }

    /**
     * This exists for use by actions which extend this class.
     *
     * @return Gets {@link TestCommand} for this action.
     */
    public TestCommand getTestCommand() {
        return testCommand;
    }

    /**
     * @param testCommand The {@link TestCommand} that will be used for this action.
     */
    public void setTestCommand(TestCommand testCommand) {
        this.testCommand = testCommand;
    }

    /**
     * @return Gets the program that is running this action.
     */
    public Program getProgram() {
        return program;
    }

    /**
     * Directly find a parameter via it's name, ths cannot be a child parameter.
     *
     * @param parameterName the parameter name we want to find.
     * @return The found parameter - need to check if it exists before use.
     */
    public TestParameter getParameterByName(String parameterName) {
        return getTestCommand().getParameterByName(parameterName);
    }

    /**
     * Check to see if a parameter exists.
     *
     * @param path The path to the parameter we want.
     * @return Boolean showing if this parameter exists or not.
     */
    public Boolean parameterByPathExists(String path) {
        return getTestCommand().getParameterByPath(path).exists();
    }

    /**
     * This acts as a shortcut to allow a path to be returned
     *
     * @param path The path to the parameter we want.
     * @return The found parameter - need to check if it exists before use.
     */
    public TestParameter getParameterByPath(String path) {
        return getTestCommand().getParameterByPath(path);
    }

    /**
     * This function takes a TestParameter object and returns a selenium 'By' object based on how the element is referenced.
     * <p>
     * Both XPath and element Id are handled automatically.
     *
     * @param elementToFind The {@link TestParameter} that contains the references to the elements and the method that we went to find.
     * @return Returns a Selenium {@link org.openqa.selenium.By} which can be used to find the element.
     */
    public By findElement(TestParameter elementToFind) {
        if ("xPath".equals(elementToFind.getParameterName())) {
            return By.xpath(elementToFind.getParameterValue());
        } else if ("id".equals(elementToFind.getParameterName())) {
            return By.id(elementToFind.getParameterValue());
        } else if ("name".equals(elementToFind.getParameterName())) {
            return By.name(elementToFind.getParameterValue());
        }

        return null;
    }

    // Uses better element caching to hopefully speed up any tests
    public SmartSpecifiedElement smartSpecifiedElement() {
        TestParameter idElement = getTestCommand().getParameterByName("id");
        TestParameter nameElement = getTestCommand().getParameterByName("name");
        TestParameter xPathElement = getTestCommand().getParameterByName("xPath");
        TestParameter loopElement = getTestCommand().getParameterByName("loop");

        WebDriver webDriver = getDriver();
        LoopTracker loopTracker = getLoopTracker();
        DocumentTracker documentTracker = getDocumentTracker();

        return new SmartSpecifiedElement(idElement, nameElement, xPathElement, loopElement, webDriver, loopTracker, documentTracker);
    }

    public WebElement specifiedElement() {
        TestParameter idElement = getTestCommand().getParameterByName("id");
        TestParameter nameElement = getTestCommand().getParameterByName("name");
        TestParameter xPathElement = getTestCommand().getParameterByName("xPath");
        TestParameter loopElement = getTestCommand().getParameterByName("loop");

        WebElement testElement = null;
        if (idElement.exists()) { // Get the element via id
            testElement = getDriver().findElement(findElement(idElement));
        } else if (xPathElement.exists()) { // Get the element via xPath
            testElement = getDriver().findElement(findElement(xPathElement));
        } else if (loopElement.exists()) { // Get element via loop
            if (getLoopTracker().getLoop(loopElement.getParameterValue()) != null) {
                LoopedWebElement loopedWebElement = (LoopedWebElement) getLoopTracker().getLoop(loopElement.getParameterValue()).getCurrentLoopObject();
                if (loopedWebElement != null) {
                    testElement = loopedWebElement.getWebElement(getDriver());
                }
            }
        } else if (nameElement.exists()) { // Get element via name
            testElement = getDriver().findElement(findElement(nameElement));
        }

        return testElement;
    }

    public By specifiedBy() {
        TestParameter idElement = getTestCommand().getParameterByName("id");
        TestParameter nameElement = getTestCommand().getParameterByName("name");
        TestParameter xPathElement = getTestCommand().getParameterByName("xPath");

        By by = null;
        if (idElement.exists() || xPathElement.exists() || nameElement.exists()) { // Get the element via id
            by = findElement(idElement);
        }

        return by;
    }

    public Test getRunningTest() {
        return runningTest;
    }

    public IfTracker getIfTracker() {
        return ifTracker;
    }

    public FunctionTracker getFunctionTracker() {
        return functionTracker;
    }

    public StateTracker getStateTracker() {
        return stateTracker;
    }

    public LoopTracker getLoopTracker() {
        return loopTracker;
    }

    public VariableTracker getVariableTracker() {
        return variableTracker;
    }

    public DocumentTracker getDocumentTracker() {
        return documentTracker;
    }

    public TestRunner getTestRunner() {
        return testRunner;
    }
}
