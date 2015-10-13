package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import application.test.action.helpers.LoopTracker;
import application.test.action.helpers.LoopedWebElement;
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
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter idElement = getTestCommand().getParameterByName("id");
        TestParameter xPathElement = getTestCommand().getParameterByName("xPath");
        TestParameter message = getTestCommand().getParameterByName("message");
        TestParameter loopElement = getTestCommand().getParameterByName("loop");

        if (message.exists()) {
            log.info(message.getParameterValue());
            testStep.setTestString(getTestCommand().getRawCommand());
        } else if (idElement.exists() || xPathElement.exists()) {
            String xPath = null;
            if (xPathElement.exists()) {
                xPath = xPathElement.getParameterValue();
            } else if (idElement.exists()) {
                xPath = "//*[@id=\"" + idElement.getParameterValue() + "\"]";
            }

            if (xPath != null) {
                Element testElement = SDEUtils.getElementFromXPath(xPath, getCurrentDocument());
                processElement(testElement, testStep);

            }
        } else if (loopElement.exists()) {
            Element loopedElement = null;
            LoopedWebElement loopedWebElement = LoopTracker.getLoop(loopElement.getParameterValue()).getCurrentLoopWebElement();
            if (loopedWebElement != null) {
                loopedElement = loopedWebElement.getElement();
            }

            processElement(loopedElement, testStep);
        }

        DataBank.saveTestStep(testStep);
    }

    private void processElement(Element element, TestStep testStep) {
        //takeScreenshotOfElement(testStep, webElement);
        testStep.setTestString(getTestCommand().getRawCommand());

        if (element != null) {
            log.info(element.outerHtml());
        } else {
            log.info("Null Element passed to LogAction");
        }
    }
}
