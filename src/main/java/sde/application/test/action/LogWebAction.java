package sde.application.test.action;

import sde.application.test.TestParameter;
import sde.application.test.action.helpers.Variable;
import sde.application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebElement;

/**
 * This action logs a message, it can also log an element variable.
 */
public class LogWebAction extends WebAction {

    private static Logger log = Logger.getLogger(LogWebAction.class);

    public LogWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter variable = getTestCommand().getParameterByName("var");
        TestParameter message = getTestCommand().getParameterByName("message");

        if (message.exists()) {
            log.info(message.getParameterValue());
            if (getRunningTest() != null && getRunningTest().getTestCase() != null) {
                getRunningTest().getTestCase().log(message.getParameterValue());
            }
        } else if (variable.exists()) {
            Variable var = getVariableTracker().getVariable(variable.getParameterValue());
            if (var != null) {
                log.info(var.getVariableValue());
            }
        } else {
            processElement(specifiedElement());
        }
    }

    private void processElement(WebElement webElement) {
        if (webElement != null) {
            Element element = SDEUtils.getJSoupElementFromWebElement(webElement, getDocumentTracker().getCurrentDocument());
            log.info(element.outerHtml());
            if (getRunningTest() != null && getRunningTest().getTestCase() != null) {
                getRunningTest().getTestCase().log(element.outerHtml());
            }
        } else {
            log.info("Null Element passed to LogAction");
            if (getRunningTest() != null && getRunningTest().getTestCase() != null) {
                getRunningTest().getTestCase().log("Null Element passed to LogAction");
            }
        }
    }
}
