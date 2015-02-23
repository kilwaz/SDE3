package application.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TestCommand {

    String mainCommand = "";
    HashMap<String, TestParameter> parameters = new HashMap<>();

    public TestCommand(String mainCommand) {
        this.mainCommand = mainCommand;
    }

    public void addParameter(TestParameter testParameter) {
        parameters.put(testParameter.getParameterName(), testParameter);
    }

    public String getMainCommand() {
        return mainCommand;
    }

    public HashMap<String, TestParameter> getParameters() {
        return parameters;
    }

    public TestParameter getParameterByName(String parameterName) {
        return parameters.get(parameterName);
    }

    public TestParameter getParameterByPath(String parameterPath) {
        List<String> path = new ArrayList<>();
        Collections.addAll(path, parameterPath.split("::"));

        TestParameter currentParameter = null;

        for (String pathToken : path) {
            if (currentParameter == null) {
                currentParameter = parameters.get(pathToken);
                if (currentParameter == null) { // We cannot find the path within our own
                    return null;
                }
            } else {
                if (currentParameter.getChildParameter().getParameterName().equals(pathToken)) { // Go to the next matching child down the chain
                    currentParameter = currentParameter.getChildParameter();
                } else {  // The path is incorrect so we return as far as we have made it
                    return null;
                }
            }
        }

        return currentParameter;
    }

    public static TestCommand parseCommand(String command) {
        TestCommand newCommand = new TestCommand(command.substring(0, command.indexOf(">")));

        List<String> parameters = new ArrayList<>();
        Collections.addAll(parameters, command.split(">"));

        for (String parameter : parameters) {
            TestParameter newTestParameter = TestParameter.parseParameter(parameter);
            if (newTestParameter != null) { // Null if parameter does not contain ::
                newCommand.addParameter(newTestParameter);
            }
        }

        return newCommand;
    }
}
