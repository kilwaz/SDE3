package application.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * The test command holds a parsed version of the script commend to that it can be handled easily.
 * <p>
 * The parameters are parsed and kept available to be queried in this object.
 */
public class TestCommand {
    String mainCommand = "";
    String rawCommand = "";
    HashMap<String, TestParameter> parameters = new HashMap<>();

    /**
     * @param mainCommand The parsed main command.
     */
    public TestCommand(String mainCommand) {
        this.mainCommand = mainCommand;
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

        TestParameter currentParameter = new TestParameter();

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

        return currentParameter;
    }

    /**
     * This method converts the command written in the test script into usable objects.
     *
     * @param command The full command as written in the script.
     * @return A new TestCommand containing the parsed command and any parameters.
     */
    public static TestCommand parseCommand(String command) {
        if (command.contains(">")) { // If the command does not have '>' then it is not a valid command, we skip this
            TestCommand newCommand = new TestCommand(command.substring(0, command.indexOf(">")));
            newCommand.setRawCommand(command);

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
}
