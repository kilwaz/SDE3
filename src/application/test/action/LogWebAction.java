package application.test.action;

import application.test.TestParameter;
import application.test.action.helpers.LoopedWebElement;
import application.test.action.helpers.Variable;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

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
        TestParameter idElement = getTestCommand().getParameterByName("id");
        TestParameter xPathElement = getTestCommand().getParameterByName("xPath");
        TestParameter loopElement = getTestCommand().getParameterByName("loop");
        TestParameter variable = getTestCommand().getParameterByName("var");

        TestParameter message = getTestCommand().getParameterByName("message");

        if (message.exists()) {
            log.info(message.getParameterValue());
            if (getRunningTest() != null && getRunningTest().getTestCase() != null) {
                getRunningTest().getTestCase().log(message.getParameterValue());
            }
        } else if (idElement.exists() || xPathElement.exists()) {
            String xPath = null;
            if (xPathElement.exists()) {
                xPath = xPathElement.getParameterValue();
            } else if (idElement.exists()) {
                xPath = "//*[@id=\"" + idElement.getParameterValue() + "\"]";
            }

            if (xPath != null) {
                Element testElement = SDEUtils.getElementFromXPath(xPath, getDocumentTracker().getCurrentDocument());
                processElement(testElement);

            }
        } else if (loopElement.exists()) {
            Element loopedElement = null;
            LoopedWebElement loopedWebElement = (LoopedWebElement) getLoopTracker().getLoop(loopElement.getParameterValue()).getCurrentLoopObject();
            if (loopedWebElement != null) {
                loopedElement = loopedWebElement.getElement();
            }

            processElement(loopedElement);
        } else if (variable.exists()) {
            Variable var = getVariableTracker().getVariable(variable.getParameterValue());
            if (var != null) {
                log.info(var.getVariableValue());
            }
        }
    }

    private void processElement(Element element) {
        if (element != null) {
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
