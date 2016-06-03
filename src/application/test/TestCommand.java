package application.test;

import application.data.model.DatabaseObject;
import application.error.Error;
import application.node.objects.Test;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.joda.time.DateTime;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * The test command holds a parsed version of the script commend to that it can be handled easily.
 * <p>
 * The parameters are parsed and kept available to be queried in this object.
 */
public class TestCommand extends DatabaseObject {
    private SimpleStringProperty mainCommand = new SimpleStringProperty();
    private String rawCommand = "";
    private HashMap<String, TestParameter> parameters = new HashMap<>();
    private Integer commandPosition = -1;
    private Test parentTest = null;
    private BufferedImage screenshot = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private Boolean hasScreenshot = false;
    private DateTime commandDate = null;

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

    public BufferedImage getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(BufferedImage screenshot) {
        this.screenshot = screenshot;
        this.hasScreenshot = true;
    }

    // Returns an input stream from the current screenshot
    public InputStream getScreenshotInputStream() {
        InputStream inputStream = null;
        if (screenshot != null) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(screenshot, "png", os);
                inputStream = new ByteArrayInputStream(os.toByteArray());
            } catch (IOException ex) {
                Error.RETRIEVE_SCREENSHOT.record().create(ex);
            }
        }

        return inputStream;
    }

    public Integer getCommandPosition() {
        return this.commandPosition;
    }

    public void setCommandPosition(Integer commandPosition) {
        this.commandPosition = commandPosition;
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
     * @return Main command.
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
