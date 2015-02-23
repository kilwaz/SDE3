package application.test;

/**
 * Created by alex on 18/02/2015.
 */
public class TestParameter {
    private String parameterName;
    private String parameterValue;
    private TestParameter childParameter;

    public TestParameter(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setChildParameter(TestParameter childParameter) {
        this.childParameter = childParameter;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public TestParameter getChildParameter() {
        return childParameter;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public static TestParameter parseParameter(String parameter) {
        if (parameter.contains("::")) {
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
