package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import application.test.action.helpers.*;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

/**
 *  This action is used to start and end an if statement
 *
 *  Various elements can be checked to see if they equal a value or if it exists or contains certain text
 */
public class IfWebAction extends WebAction {
    private static Logger log = Logger.getLogger(IfWebAction.class);

    public IfWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestStep testStep  =  TestStep.create(TestStep.class);
        testStep.setParentResult(getTestResult());
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
        TestParameter elementExists = getTestCommand().getParameterByPath("elementExists");

        String xPath = null;
        if (elementXPath.exists()) {
            xPath = elementXPath.getParameterValue();
        } else if (elementId.exists()) {
            xPath = "//*[@id=\"" + elementId.getParameterValue() + "\"]";
        }

        Element testElement = null;
        if (elementId.exists() || elementXPath.exists()) { // Get the element via id
            testElement = SDEUtils.getElementFromXPath(xPath, getCurrentDocument());
        } else if (loopElement.exists()) { // Get element via loop
            LoopedWebElement loopedWebElement = LoopTracker.getLoop(loopElement.getParameterValue()).getCurrentLoopWebElement();
            if (loopedWebElement != null) {
                testElement = loopedWebElement.getElement();
            }
        }

        if (startElement.exists()) {
            String valueToCheck = "";
            if (elementAttribute.exists() && testElement != null) {
                valueToCheck = testElement.attr(elementAttribute.getParameterValue());
            } else if (elementClass.exists() && testElement != null) {
                valueToCheck = testElement.className();
            } else if (elementContent.exists() && testElement != null) {
                valueToCheck = testElement.html();
            } else if (variable.exists()) {
                Variable var = VariableTracker.getVariable(variable.getParameterValue());
                if (var != null) {
                    valueToCheck = var.getVariableValue();
                }
            }

            if (equals.exists() && !"".equals(valueToCheck)) {
                log.info("Comparing equals " + valueToCheck + " vs " + equals.getParameterValue());
                if (valueToCheck.equals(equals.getParameterValue())) { // TRUE

                } else {  // FALSE
                    // As it was false we now need to skip all statements until the end
                    IfTracker.setIsSkippingIf(true);
                    IfTracker.setIfReference(startElement.getParameterValue());
                }
            }

            if (contains.exists()) {
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

            if (exists.exists()) { // Test to see if a specific parameter exists
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

            if (elementExists.exists()) { // Test to see if the element exists
                if (testElement != null) {  // TRUE
                    if (elementExists.getParameterValue().equals("false")) {
                        IfTracker.setIsSkippingIf(true);
                        IfTracker.setIfReference(startElement.getParameterValue());
                    }
                } else {  // FALSE
                    if (elementExists.getParameterValue().equals("true")) {
                        IfTracker.setIsSkippingIf(true);
                        IfTracker.setIfReference(startElement.getParameterValue());
                    }
                }
            }
        }

        testStep.save();
    }
}
