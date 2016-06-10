package application.test.action;

import application.gui.Program;
import application.test.TestParameter;
import application.test.action.helpers.Variable;
import application.utils.NodeRunParams;

/**
 * This action is used to run another Node, generally a logic node.
 * <p>
 * You can pass variables into the NodeRunParams object
 */
public class RunWebAction extends WebAction {

    public RunWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter nodeToRun = getTestCommand().getParameterByName("node");
        NodeRunParams nodeRunParams = new NodeRunParams();

        for (String parameters : getTestCommand().getParameters().keySet()) {
            TestParameter testParameter = getTestCommand().getParameters().get(parameters);

            if (!"node".equals(parameters)) {
                // If a variable is being passed in we handle this here
                if (testParameter.getChildParameter() != null && testParameter.getChildParameter().exists() && "var".equals(testParameter.getChildParameter().getParameterName())) {
                    TestParameter childVariable = testParameter.getChildParameter();

                    // Converts the variable name into the variable itself
                    Variable variable = getVariableTracker().getVariable(childVariable.getParameterValue());
                    if (variable != null) {
                        nodeRunParams.addVariable(parameters, variable.getVariableValue());
                    }
                } else { // Otherwise just do whatever was passed in with names and straight string values
                    nodeRunParams.addVariable(parameters, testParameter.getParameterValue());
                }
            }
        }

        Program.runHelper(nodeToRun.getParameterValue(), getProgram().getFlowController().getReferenceID(), null, true, true, null, nodeRunParams);
    }
}





