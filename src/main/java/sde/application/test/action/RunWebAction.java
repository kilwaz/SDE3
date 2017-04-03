package sde.application.test.action;

import sde.application.gui.Program;
import sde.application.test.TestParameter;
import sde.application.test.action.helpers.LoopedWebElement;
import sde.application.test.action.helpers.Variable;
import sde.application.utils.NodeRunParams;
import org.apache.log4j.Logger;

/**
 * This action is used to run another Node, generally a logic node.
 * <p>
 * You can pass variables into the NodeRunParams object
 */
public class RunWebAction extends WebAction {
    private static Logger log = Logger.getLogger(RunWebAction.class);

    public RunWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        try {
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
                    } else if ("loop".equals(testParameter.getParameterName())) {
                        TestParameter loopElement = getParameterByName("loop");
                        if (loopElement.exists()) {
                            LoopedWebElement loopedWebElement = (LoopedWebElement) getLoopTracker().getLoop(loopElement.getParameterValue()).getCurrentLoopObject();
                            if (loopedWebElement != null) {
                                nodeRunParams.addVariable(parameters, loopedWebElement.getWebElement(getDriver()));
                            }
                        }
                    } else { // Otherwise just do whatever was passed in with names and straight string values
                        nodeRunParams.addVariable(parameters, testParameter.getParameterValue());
                    }
                }
            }

            Program.runHelper(nodeToRun.getParameterValue(), getProgram().getFlowController().getReferenceID(), null, true, true, null, nodeRunParams);
        } catch (Exception ex) {
            log.error(ex);
        }
    }
}





