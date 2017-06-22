package sde.application.test;

import sde.application.data.model.DatabaseObject;
import sde.application.node.objects.Test;
import sde.application.test.core.TestCase;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.*;

/**
 * The test command holds a parsed version of the script commend to that it can be handled easily.
 * <p>
 * The parameters are parsed and kept available to be queried in this object.
 */
public class TestCommand extends DatabaseObject {
    private static Logger log = Logger.getLogger(TestCommand.class);
    private SimpleStringProperty mainCommand = new SimpleStringProperty();
    private String rawCommand = "";
    private HashMap<String, TestParameter> parameters = new HashMap<>();
    private Integer commandLineNumber = -1;
    private Integer commandOrder = -1;
    private Test parentTest = null;
    private TestCommandScreenshot screenshot = null;
    private Boolean hasScreenshot = false;
    private DateTime commandDate = null;
    private TestCommandError testCommandError = new TestCommandError();
    private TestCase parentTestCase;

    public TestCommand() {
        super();
    }

    public TestCommand(UUID uuid, Test parentTest, String mainCommand, String rawCommand) {
        super(uuid);
        this.rawCommand = rawCommand;
        this.parentTest = parentTest;
        this.mainCommand.set(mainCommand);
    }

    /**
     * This method converts the command written in the test script into usable objects.
     *
     * @param command The full command as written in the script.
     * @return A new TestCommand containing the parsed command and any parameters.
     */
    public static TestCommand parseCommand(String command) {
        if (command.contains(">")) { // If the command does not have '>' then it is not a valid command, we skip this
            TestCommand newCommand = TestCommand.create(TestCommand.class);
            newCommand.setMainCommand(command.substring(0, command.indexOf(">")));
            newCommand.setRawCommand(command);
            newCommand.setCommandDate(new DateTime());

            List<String> parameters = new ArrayList<>();

            StringBuilder currentCommand = new StringBuilder();
            Boolean escapedData = false;
            for (Character currentLetter : command.toCharArray()) {
                if (currentCommand.toString().endsWith("[[!")) {
                    escapedData = true;
                } else if (currentCommand.toString().endsWith("!]]")) {
                    escapedData = false;
                }

                if (new Character('>').equals(currentLetter) && !escapedData) {
                    parameters.add(currentCommand.toString());
                    currentCommand = new StringBuilder();
                    continue;
                }
                currentCommand.append(currentLetter);
            }

            parameters.add(currentCommand.toString());

            for (String parameter : parameters) {
                TestParameter newTestParameter = TestParameter.parseParameter(parameter);
                if (newTestParameter != null) { // Null if parameter does not contain ::
                    newCommand.addParameter(newTestParameter);
                }
            }

            return newCommand;
        } else {
            return null;
        }
    }

    public TestCommandError getTestCommandError() {
        return testCommandError;
    }

    public void setParentTestCase(TestCase parentTestCase) {
        this.parentTestCase = parentTestCase;
    }

    public String getParentTestCaseUuid() {
        if (parentTestCase != null) {
            return parentTestCase.getUuidString();
        }
        return null;
    }

    public DateTime getCommandDate() {
        return commandDate;
    }

    public void setCommandDate(DateTime commandDate) {
        this.commandDate = commandDate;
    }

    public Boolean getHasScreenshot() {
        return hasScreenshot;
    }

    public void setHasScreenshot(Boolean hasScreenshot) {
        this.hasScreenshot = hasScreenshot;
    }

    public TestCommandScreenshot getScreenshot() {
        return screenshot;
    }

    // Sets the screenshot and then immediately offloads it to the database
    public void setTestCommandScreenshot(TestCommandScreenshot screenshot) {
        this.screenshot = screenshot;
        this.hasScreenshot = true;
    }

    public void setException(Exception exception) {
        testCommandError.setException(exception);
        if (parentTest != null && parentTest.getTestCase() != null) {
            parentTest.getTestCase().log(exception);
        }
    }

    public Boolean hasException() {
        return testCommandError.hasException();
    }

    public Integer getCommandLineNumber() {
        return this.commandLineNumber;
    }

    public void setCommandLineNumber(Integer commandLineNumber) {
        this.commandLineNumber = commandLineNumber;
    }

    public Integer getCommandOrder() {
        return commandOrder;
    }

    public void setCommandOrder(Integer commandOrder) {
        this.commandOrder = commandOrder;
    }

    public Test getParentTest() {
        return parentTest;
    }

    public void setParentTest(Test parentTest) {
        this.parentTest = parentTest;
    }

    /**
     * @param testParameter
     */
    public void addParameter(TestParameter testParameter) {
        parameters.put(testParameter.getParameterName(), testParameter);
    }

    /**
     * Gets the main command.
     *
     * @return GUI command.
     */
    public String getMainCommand() {
        return mainCommand.get();
    }

    public void setMainCommand(String mainCommand) {
        this.mainCommand.set(mainCommand);
    }

    public SimpleStringProperty mainCommandProperty() {
        return mainCommand;
    }

    /**
     * Gets the full unparsed initial command passed during creation.
     *
     * @return Raw command.
     */
    public String getRawCommand() {
        return rawCommand;
    }

    /**
     * @param rawCommand
     */
    public void setRawCommand(String rawCommand) {
        this.rawCommand = rawCommand;
    }

    /**
     * Gets the parsed parameters as objects.
     *
     * @return All of the current parameters.
     */
    public HashMap<String, TestParameter> getParameters() {
        return parameters;
    }

    /**
     * Gets a parameter directly by name.
     *
     * @param parameterName The parameter we want to find.
     * @return Found parameter if it exists, need to test for exists before using.
     */
    public TestParameter getParameterByName(String parameterName) {
        if (!parameters.containsKey(parameterName)) {
            return new TestParameter();
        } else {
            return parameters.get(parameterName);
        }
    }

    /**
     * Find a test parameter via path.
     *
     * @param parameterPath The specific path to the test parameter.
     * @return Found test parameter via path, it should be checked to see if it exists before usage.
     */
    public TestParameter getParameterByPath(String parameterPath) {
        List<String> path = new ArrayList<>();
        Collections.addAll(path, parameterPath.split("::"));

        TestParameter currentParameter = null;

        for (String pathToken : path) {
            if (currentParameter == null) {
                currentParameter = parameters.get(pathToken);
                if (currentParameter == null) { // We cannot find the path within our own
                    return new TestParameter(); // This returns an empty test parameter object
                }
            } else {
                if (currentParameter.getChildParameter() != null && currentParameter.getChildParameter().getParameterName().equals(pathToken)) { // Go to the next matching child down the chain
                    currentParameter = currentParameter.getChildParameter();
                } else {  // The path is incorrect so we return as far as we have made it
                    return new TestParameter(); // This returns an empty test parameter object
                }
            }
        }

        if (currentParameter == null) {
            currentParameter = new TestParameter();
        }

        return currentParameter;
    }

    public ObservableList<TestParameter> getTestParameters() {
        ObservableList<TestParameter> list = FXCollections.observableArrayList();
        for (TestParameter testParameter : parameters.values()) {
            list.add(testParameter);
            while (testParameter.getChildParameter() != null) {
                testParameter = testParameter.getChildParameter();
                list.add(testParameter);
            }
        }
        return list;
    }

    public String getParentUuid() {
        if (parentTest != null) {
            return parentTest.getUuidString();
        }
        return null;
    }
}
