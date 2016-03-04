package application.test.action;

import application.test.TestParameter;
import application.test.TestStep;
import application.test.action.helpers.LoopedWebElement;
import application.test.action.helpers.Variable;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebElement;

/**
 * This action is used to start and end an if statement
 * <p>
 * Various elements can be checked to see if they equal a value or if it exists or contains certain text
 */
public class IfWebAction extends WebAction {
    private static Logger log = Logger.getLogger(IfWebAction.class);

    public IfWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestStep testStep = TestStep.create(TestStep.class);
        testStep.setParentResult(getTestResult());
        getTestResult().addTestStep(testStep);

        // Element References
        TestParameter startElement = getTestCommand().getParameterByName("start");
        TestParameter loopElement = getTestCommand().getParameterByPath("loop");
        TestParameter elementId = getTestCommand().getParameterByPath("id");
        TestParameter elementXPath = getTestCommand().getParameterByPath("xPath");
        TestParameter elementAttribute = getTestCommand().getParameterByPath("attribute");
        TestParameter elementClass = getTestCommand().getParameterByPath("class");
        TestParameter elementContent = getTestCommand().getParameterByPath("content");
        TestParameter variable = getTestCommand().getParameterByPath("var");

        // Conditions
        TestParameter equals = getTestCommand().getParameterByPath("equals");
        TestParameter contains = getTestCommand().getParameterByPath("contains");
        TestParameter exists = getTestCommand().getParameterByPath("exists");
        TestParameter elementExists = getTestCommand().getParameterByPath("elementExists");
        TestParameter elementVisible = getTestCommand().getParameterByPath("elementVisible");

        String xPath = null;
        if (elementXPath.exists()) {
            xPath = elementXPath.getParameterValue();
        } else if (elementId.exists()) {
            xPath = "//*[@id=\"" + elementId.getParameterValue() + "\"]";
        }

        Element testElement = null;
        WebElement testWebElement = null;
        if (elementId.exists() || elementXPath.exists()) { // Get the element via id
            testElement = SDEUtils.getElementFromXPath(xPath, getCurrentDocument());
        } else if (loopElement.exists()) { // Get element via loop
            LoopedWebElement loopedWebElement = (LoopedWebElement) getLoopTracker().getLoop(loopElement.getParameterValue()).getCurrentLoopObject();
            if (loopedWebElement != null) {
                testElement = loopedWebElement.getElement();
                testWebElement = loopedWebElement.getWebElement(getDriver());
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
                Variable var = getVariableTracker().getVariable(variable.getParameterValue());
                if (var != null) {
                    if (var.getVariableValue() instanceof String) {
                        valueToCheck = (String) var.getVariableValue();
                    }
                }
            } else if (contains.exists() && testElement != null) {
                valueToCheck = testElement.html();
            }

            if (equals.exists() && !"".equals(valueToCheck)) {
                //log.info("Comparing equals " + valueToCheck + " vs " + equals.getParameterValue());
                if (valueToCheck.equals(equals.getParameterValue())) { // TRUE

                } else {  // FALSE
                    // As it was false we now need to skip all statements until the end
                    getIfTracker().setIsSkippingIf(true);
                    getIfTracker().setIfReference(startElement.getParameterValue());
                }
            }

            if (contains.exists()) {
                if ("".equals(valueToCheck)) { // FALSE
                    // If no element was found (so valueToCheck is "") then it does not contain our text, return false
                    getIfTracker().setIsSkippingIf(true);
                    getIfTracker().setIfReference(startElement.getParameterValue());
                } else {
                    if (valueToCheck.contains(contains.getParameterValue())) { // TRUE

                    } else {  // FALSE
                        // As it was false we now need to skip all statements until the end
                        getIfTracker().setIsSkippingIf(true);
                        getIfTracker().setIfReference(startElement.getParameterValue());
                    }
                }
            }

            if (exists.exists()) { // Test to see if a specific parameter exists
                if (!"".equals(valueToCheck)) {  // TRUE
                    if (exists.getParameterValue().equals("false")) {
                        getIfTracker().setIsSkippingIf(true);
                        getIfTracker().setIfReference(startElement.getParameterValue());
                    }
                } else {  // FALSE
                    if (exists.getParameterValue().equals("true")) {
                        getIfTracker().setIsSkippingIf(true);
                        getIfTracker().setIfReference(startElement.getParameterValue());
                    }
                }
            }

            if (elementExists.exists()) { // Test to see if the element exists
                if (testElement != null) {  // TRUE
                    if (elementExists.getParameterValue().equals("false")) {
                        getIfTracker().setIsSkippingIf(true);
                        getIfTracker().setIfReference(startElement.getParameterValue());
                    }
                } else {  // FALSE
                    if (elementExists.getParameterValue().equals("true")) {
                        getIfTracker().setIsSkippingIf(true);
                        getIfTracker().setIfReference(startElement.getParameterValue());
                    }
                }
            }

            if (elementVisible.exists()) { // Test to see if the element is currently visible
                if (testElement != null && testWebElement != null && testWebElement.isDisplayed()) {  // TRUE
                    if (elementVisible.getParameterValue().equals("false")) {
                        getIfTracker().setIsSkippingIf(true);
                        getIfTracker().setIfReference(startElement.getParameterValue());
                    }
                } else {  // FALSE
                    getIfTracker().setIsSkippingIf(true);
                    getIfTracker().setIfReference(startElement.getParameterValue());
                }
            }
        }

        testStep.save();
    }
}
