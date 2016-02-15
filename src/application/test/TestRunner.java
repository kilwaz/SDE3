package application.test;

import application.gui.Program;
import application.net.proxy.snoop.HttpProxyServer;
import application.node.objects.Test;
import application.test.action.WebAction;
import application.test.action.helpers.*;
import application.utils.BrowserHelper;
import application.utils.SDERunnable;
import application.utils.SDEThread;
import com.jayway.awaitility.Awaitility;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.SessionNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestRunner extends SDERunnable {
    public static Integer TEST_NOT_STARTED = 0;
    public static Integer TEST_PENDING = 1;
    public static Integer TEST_RUNNING = 2;
    public static Integer TEST_FINISHED = 3;
    private static Logger log = Logger.getLogger(TestRunner.class);
    private Test test;
    private Program program;
    private Integer status = 0;

    public TestRunner(Test test, Program program) {
        this.test = test;
        this.program = program;
    }

    public void run() {
        status = TEST_RUNNING;
        if (test != null) {
            TestResult testResult = TestResult.create(TestResult.class);
            if (testResult != null) {
                testResult.save();
            }

            List<String> commands = new ArrayList<>();
            Collections.addAll(commands, test.getText().split("[\\r\\n]"));

            IfTracker ifTracker = new IfTracker();
            FunctionTracker functionTracker = new FunctionTracker();
            LoopTracker loopTracker = new LoopTracker();
            VariableTracker variableTracker = new VariableTracker();
            StateTracker stateTracker = new StateTracker();

            String remoteDriverURL = "";
            String browser = "chrome";
            Boolean useLocalDriver = true;

            // Finds the functions within the script
            test.setCurrentLine(0);
            functionTracker.cleanFunctions();
            for (String command : commands) {
                // We need to trim this to remove spaces and tabs
                if (command.startsWith("//") || command.equals("")) { // Ignore the command if it is a comment
                    test.incrementLineNumber();
                    continue;
                }

                TestCommand testCommand = TestCommand.parseCommand(command);
                if (testCommand != null && "function".equals(testCommand.getMainCommand())) {
                    TestParameter functionStart = testCommand.getParameterByPath("start");
                    if (functionStart.exists()) {
                        functionTracker.addFunction(functionStart.getParameterValue(), test.getCurrentLine());
                    }
                } else if (testCommand != null && "driver".equals(testCommand.getMainCommand())) { // Find the driver configuration if there is any
                    TestParameter driverRemoteURL = testCommand.getParameterByPath("location::remoteURL");
                    TestParameter driverBrowser = testCommand.getParameterByPath("browser");
                    if (driverRemoteURL.exists()) {
                        useLocalDriver = false;
                        remoteDriverURL = driverRemoteURL.getParameterValue();
                    }
                    if (driverBrowser.exists()) {
                        browser = driverBrowser.getParameterValue();
                    }
                }

                test.incrementLineNumber();
            }

            test.setCurrentLine(0);

            // Creates the WebProxy used for this node
            HttpProxyServer httpProxyServer = new HttpProxyServer();
            new SDEThread(httpProxyServer, "Running test");
            Awaitility.await().atMost(60000, TimeUnit.MILLISECONDS).until(httpProxyServer.nowConnected());

            browser = browser.toLowerCase();

            if (useLocalDriver) {
                log.info("Using local driver with browser " + browser);
            } else {
                log.info("Using remote driver at " + remoteDriverURL + " with browser " + browser);
            }

            WebDriver driver = null;
            if (useLocalDriver) {
                if ("chrome".equals(browser)) {
                    driver = BrowserHelper.getChrome(httpProxyServer.getConnectionString());
                } else if ("firefox".equals(browser)) {
                    driver = BrowserHelper.getFireFox(httpProxyServer.getConnectionString());
                } else if ("ie".equals(browser)) {
                    driver = BrowserHelper.getIE(httpProxyServer.getConnectionString());
                } else if ("opera".equals(browser)) {
                    driver = BrowserHelper.getOpera(httpProxyServer.getConnectionString());
                }
            } else {
                if ("chrome".equals(browser)) {
                    driver = BrowserHelper.getRemoteChrome("172.16.10.208:" + httpProxyServer.getRunningPort(), remoteDriverURL);
                } else if ("firefox".equals(browser)) {
                    log.info("No remote firefox has been configured");
                } else if ("ie".equals(browser)) {
                    log.info("No remote ie has been configured");
                } else if ("opera".equals(browser)) {
                    log.info("No remote operae has been configured");
                }
            }

            log.info("Number of commands in test " + commands.size());

            test.setContinueTest(true);
            while (test.getCurrentLine() < commands.size() && test.getContinueTest()) {
                String command = commands.get(test.getCurrentLine()).trim(); // We need to trim this to remove spaces and tabs

                if (command.startsWith("//") || command.equals("")) { // Ignore the command if it is a comment
                    test.incrementLineNumber();
                    continue;
                }

                TestCommand testCommand = TestCommand.parseCommand(command);

                // Here we are checking if an if statement is currently happening, if so we need to move to end if statement
                if (ifTracker.isSkippingIf()) {  // Maybe move this so somewhere else?
                    if (command.equals("if>end::" + ifTracker.getIfReference())) {
                        ifTracker.setIsSkippingIf(false);
                    }
                    test.incrementLineNumber();
                } else if (functionTracker.isSkippingFunction()) {
                    if (command.equals("function>end::" + functionTracker.getFunctionReference())) {
                        functionTracker.setIsSkippingFunction(false);
                    }
                    test.incrementLineNumber();
                } else { // If no if is being skipped we continue as normal
                    log.info("(" + test.toString() + ") - Command " + command);

                    // If the user is viewing the node at the time we can select the line that is currently being run
//                    if (aceTextArea != null) {
//                        aceTextArea.goToLine(test.getCurrentLine() + 1);
//                    }
                    test.incrementLineNumber();

                    // Here we are retrieving the correct class held within ActionControl mapping (within application.test.action)
                    // and initialising the object and performing the required action which is then handled by the object
                    if (testCommand != null) {
                        if (driver != null) {
                            try {
                                Class actionClass = WebAction.getClassMapping(testCommand.getMainCommand());
                                WebAction webAction = (WebAction) actionClass.getDeclaredConstructor().newInstance();
                                webAction.initialise(httpProxyServer, driver, testCommand, testResult, program, test, ifTracker, functionTracker, loopTracker, variableTracker, stateTracker);
                                webAction.performAction();
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                                application.error.Error.TEST_NODE_ACTION.record().create(ex);
                            }
                        } else {
                            test.setContinueTest(false);
                            application.error.Error.NO_BROWSER_FOUND.record().additionalInformation("Browser set to: " + browser).create();
                        }
                    }
                }
            }

            // Tidy up any resources if they are still in use
            try {
                if (driver != null) {
                    driver.close();
                    driver.quit();
                }
            } catch (SessionNotFoundException ex) {
                // If this throws an exception that is fine, we don't need to do anything with it
                // It just means that the driver has already closed
            }

            httpProxyServer.close();

            // Doing something with the screenshots
//            Integer counter = 1;
//            for (TestStep testStep : testResultReloaded.getTestSteps()) {
//                try {
//                    ImageIO.write(testStep.getScreenshot(), "png", new File("C:\\Users\\alex\\Desktop\\TestStep" + testResultReloaded.getId() + "-" + counter + ".png"));
//                } catch (IOException ex) {
//                    log.error(ex);
//                }
//                counter++;
//            }
        }
        status = TEST_FINISHED;
    }

    public String getStatusText() {
        if (status.equals(TEST_NOT_STARTED)) {
            return "Not started";
        } else if (status.equals(TEST_PENDING)) {
            return "Pending";
        } else if (status.equals(TEST_RUNNING)) {
            return "Running";
        } else if (status.equals(TEST_FINISHED)) {
            return "Finished";
        }
        return "Status unknown";
    }

    public Integer getStatus() {
        return status;
    }

    public Test getTest() {
        return test;
    }
}
