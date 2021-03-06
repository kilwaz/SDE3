package sde.application.test.action;

import sde.application.test.TestParameter;
import sde.application.test.action.helpers.LoopedWebElement;
import sde.application.test.action.helpers.Variable;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import sde.application.test.action.helpers.VariableTracker;

/**
 * This class is used to save variables within the test scripts
 */

public class SetWebAction extends WebAction {

    private static Logger log = Logger.getLogger(SetWebAction.class);

    public SetWebAction() {
    }

    /**
     * The action performed here is to save a variable or overwrite an existing one with the same name with the provided
     * 'value'.
     * <p>
     * These values are saved and can be retrieved from the {@link VariableTracker}.
     */
    public void performAction() {
        TestParameter variableName = getTestCommand().getParameterByName("var");
        TestParameter variableStringValue = getTestCommand().getParameterByPath("value::string");
        TestParameter variableContent = getTestCommand().getParameterByName("content");
        TestParameter variableLoop = getTestCommand().getParameterByPath("value::loop");

        if (variableStringValue.exists()) {
            getVariableTracker().setVariable(new Variable(variableName.getParameterValue(), variableStringValue.getParameterValue()));
        } else if (variableLoop.exists()) {
            LoopedWebElement loopedWebElement = (LoopedWebElement) getLoopTracker().getLoop(variableLoop.getParameterValue()).getCurrentLoopObject();
            Element loopedElement = loopedWebElement.getElement();

            if (variableContent.exists()) {
                getVariableTracker().setVariable(new Variable(variableName.getParameterValue(), loopedElement.html()));
            }
        }
    }
}
