package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import application.test.action.helpers.*;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

public class IfAction extends ActionControl {
    private static Logger log = Logger.getLogger(IfAction.class);

    public IfAction() {
    }

    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter startElement = getTestCommand().getParameterByName("start");
        TestParameter loopElement = getTestCommand().getParameterByPath("loop");
        TestParameter elementId = getTestCommand().getParameterByPath("id");
        TestParameter elementXPath = getTestCommand().getParameterByPath("xPath");
        TestParameter elementAttribute = getTestCommand().getParameterByPath("attribute");
        TestParameter elementClass = getTestCommand().getParameterByPath("class");
        TestParameter elementContent = getTestCommand().getParameterByPath("content");
        TestParameter variable = getTestCommand().getParameterByPath("var");

        TestParameter equals = getTestCommand().getParameterByPath("equals");
        TestParameter contains = getTestCommand().getParameterByPath("contains");
        TestParameter exists = getTestCommand().getParameterByPath("exists");

        String xPath = null;
        if (elementXPath != null) {
            xPath = elementXPath.getParameterValue();
        } else if (elementId != null) {
            xPath = "//*[@id=\"" + elementId.getParameterValue() + "\"]";
        }

        Element testElement = null;
        if (elementId != null || elementXPath != null) { // Get the element via id
            testElement = SDEUtils.getElementFromXPath(xPath, getCurrentDocument());
        } else if (loopElement != null) { // Get element via loop
            LoopedWebElement loopedWebElement = LoopTracker.getLoop(loopElement.getParameterValue()).getCurrentLoopWebElement();
            if (loopedWebElement != null) {
                testElement = loopedWebElement.getElement();
            }
        }

        if (startElement != null) {
            String valueToCheck = "";
            if (elementAttribute != null && testElement != null) {
                valueToCheck = testElement.attr(elementAttribute.getParameterValue());
            } else if (elementClass != null && testElement != null) {
                valueToCheck = testElement.className();
            } else if (elementContent != null && testElement != null) {
                valueToCheck = testElement.html();
            } else if (variable != null) {
                Variable var = VariableTracker.getVariable(variable.getParameterValue());
                if (var != null) {
                    valueToCheck = var.getVariableValue();
                }
            }

            if (equals != null && !"".equals(valueToCheck)) {
                log.info("Comparing equals " + valueToCheck + " vs " + equals.getParameterValue());
                if (valueToCheck.equals(equals.getParameterValue())) { // TRUE

                } else {  // FALSE
                    // As it was false we now need to skip all statements until the end
                    IfTracker.setIsSkippingIf(true);
                    IfTracker.setIfReference(startElement.getParameterValue());
                }
            }

            if (contains != null) {
                if ("".equals(valueToCheck)) { // FALSE
                    // If no element was found (so valueToCheck is "") then it does not contain our text, return false
                    IfTracker.setIsSkippingIf(true);
                    IfTracker.setIfReference(startElement.getParameterValue());
                } else {
                    if (valueToCheck.contains(contains.getParameterValue())) { // TRUE

                    } else {  // FALSE
                        // As it was false we now need to skip all statements until the end
                        IfTracker.setIsSkippingIf(true);
                        IfTracker.setIfReference(startElement.getParameterValue());
                    }
                }
            }

            if (exists != null) {
                if (!"".equals(valueToCheck)) {  // TRUE
                    if (exists.getParameterValue().equals("false")) {
                        IfTracker.setIsSkippingIf(true);
                        IfTracker.setIfReference(startElement.getParameterValue());
                    }
                } else {  // FALSE
                    if (exists.getParameterValue().equals("true")) {
                        IfTracker.setIsSkippingIf(true);
                        IfTracker.setIfReference(startElement.getParameterValue());
                    }
                }
            }
        }

        DataBank.saveTestStep(testStep);
    }
}
