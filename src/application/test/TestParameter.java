package application.test;

/**
 * This class exists as a helper to run a specific command.
 * <p>
 * A TestParameter consists of a Name:Value pair and also any children.
 * <p>
 * This is a pair as written in the script:
 * <p>
 * name::value
 * <p>
 * This is a pair with a child parameter:
 * <p>
 * name::value::child
 * <p>
 * There is no limit to how long the chain can be.
 */
public class TestParameter {
    private String parameterName = "";
    private String parameterValue = "";
    private TestParameter childParameter = null;
    private Boolean exists = false;

    /**
     * This constructor is only used when nothing was found and we want an empty parameter to report that it doesn't exist.
     */
    public TestParameter() {
    }

    /**
     * Constructs the new TestParameter with the chosen parameter name.
     * This means that the parameter exists.
     *
     * @param parameterName Name of the parameter.
     */
    public TestParameter(String parameterName) {
        this.parameterName = parameterName;
        this.exists = true;
    }

    /**
     * Gets parameter name.
     *
     * @return Parameter name.
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Sets the child parameter.
     *
     * @param childParameter The child parameter to set.
     */
    public void setChildParameter(TestParameter childParameter) {
        this.childParameter = childParameter;
    }

    /**
     * Sets the parameter value.
     *
     * @param parameterValue The parameter value.
     */
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    /**
     * Gets the parameter value.
     *
     * @return The current parameter value.
     */
    public String getParameterValue() {
        return parameterValue;
    }

    /**
     * Gets child parameter.
     *
     * @return Child parameter if any.
     */
    public TestParameter getChildParameter() {
        return childParameter;
    }

    /**
     * Sets the parameter name.
     *
     * @param parameterName The parameter name to set.
     */
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * Returns if this test parameter path actually exists.  Can check for null before using the object.
     *
     * @return Whether the path is available inside of the {@link application.test.TestCommand} or not.
     */
    public Boolean exists() {
        return exists;
    }

    /**
     * Sets exists for the object.
     *
     * @param exists If the TestParameter exists.
     */
    public void setExists(Boolean exists) {
        this.exists = exists;
    }

    /**
     * This method parses the remaining section of the command and finds any parameters that are available.
     * <p>
     * Each one will be matched up and created.  If a parameter has further sections inside of it they will be recursively
     * parsed until there are no more.
     *
     * @param parameter Parameter string to parse.
     * @return The newly created {@link application.test.TestParameter}.
     */
    public static TestParameter parseParameter(String parameter) {
        if (parameter.contains("::")) {

            // These are the start and end sections used to to escape.
            parameter = parameter.replace("[[!", "");
            parameter = parameter.replace("!]]", "");

            String parameterName = parameter.substring(0, parameter.indexOf("::"));

            TestParameter testParameter = new TestParameter(parameterName);
            TestParameter childParameter = TestParameter.parseParameter(parameter.substring(parameter.indexOf("::") + 2));
            if (childParameter != null) { // If the parameter chain is longer we create that..
                testParameter.setChildParameter(childParameter);
            } else { // Otherwise we have found the token value at the end of the parameter chain
                testParameter.setParameterValue(parameter.substring(parameter.indexOf("::") + 2));
            }

            return testParameter;
        } else {
            return null;
        }
    }
}
