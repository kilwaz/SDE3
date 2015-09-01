package application.test.action;

import application.data.DataBank;
import application.gui.Program;
import application.test.TestParameter;
import application.test.TestStep;
import application.test.action.helpers.Variable;
import application.test.action.helpers.VariableTracker;
import application.utils.NodeRunParams;

public class RunAction extends ActionControl {
    // This class is used to run another node part way through a test script
    public RunAction() {
    }

    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter nodeToRun = getTestCommand().getParameterByName("node");

        NodeRunParams nodeRunParams = new NodeRunParams();

        for (String parameters : getTestCommand().getParameters().keySet()) {
            TestParameter testParameter = getTestCommand().getParameters().get(parameters);

            if (!"node".equals(parameters)) {
                // If a variable is being passed in we handle this here
                if (testParameter.getChildParameter() != null && "var".equals(testParameter.getChildParameter().getParameterName())) {
                    TestParameter childVariable = testParameter.getChildParameter();

                    // Converts the variable name into the variable itself
                    Variable variable = VariableTracker.getVariable(childVariable.getParameterValue());
                    if (variable != null) {
                        nodeRunParams.addVariable(parameters, variable.getVariableValue());
                    }
                } else { // Otherwise just do whatever was passed in with names and straight string values
                    nodeRunParams.addVariable(parameters, testParameter.getParameterValue());
                }
            }
        }

        Program.runHelper(nodeToRun.getParameterValue(), DataBank.currentlyEditProgram.getFlowController().getReferenceID(), null, true, false, nodeRunParams);

        DataBank.saveTestStep(testStep);
    }
}





