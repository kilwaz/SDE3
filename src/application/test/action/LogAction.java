package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import application.test.action.helpers.LoopTracker;
import application.test.action.helpers.LoopedWebElement;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

public class LogAction extends ActionControl {

    private static Logger log = Logger.getLogger(LogAction.class);

    // This class is used to input a value into an element
    public LogAction() {
    }

    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter idElement = getTestCommand().getParameterByName("id");
        TestParameter xPathElement = getTestCommand().getParameterByName("xPath");
        TestParameter message = getTestCommand().getParameterByName("message");
        TestParameter loopElement = getTestCommand().getParameterByName("loop");

        if (message != null) {
            log.info(message.getParameterValue());
            testStep.setTestString(getTestCommand().getRawCommand());
        } else if (idElement != null || xPathElement != null) {
            String xPath = null;
            if (xPathElement != null) {
                xPath = xPathElement.getParameterValue();
            } else if (idElement != null) {
                xPath = "//*[@id=\"" + idElement.getParameterValue() + "\"]";
            }

            if (xPath != null) {
                Element testElement = SDEUtils.getElementFromXPath(xPath, getCurrentDocument());
                processElement(testElement, testStep);

            }
        } else if (loopElement != null) {
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
