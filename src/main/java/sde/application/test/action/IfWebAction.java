package sde.application.test.action;

import sde.application.test.TestParameter;
import sde.application.test.action.helpers.SmartSpecifiedElement;
import sde.application.test.action.helpers.Variable;
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

    private TestParameter startElement;
    private TestParameter not;

    public IfWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        // Element References
        startElement = getTestCommand().getParameterByName("start");
        TestParameter elementAttribute = getTestCommand().getParameterByPath("attribute");
        TestParameter elementClass = getTestCommand().getParameterByPath("class");
        TestParameter elementContent = getTestCommand().getParameterByPath("content");
        TestParameter variable = getTestCommand().getParameterByPath("var");

        // Conditions
        TestParameter equals = getTestCommand().getParameterByPath("equals");
        TestParameter contains = getTestCommand().getParameterByPath("contains");
        TestParameter exists = getTestCommand().getParameterByPath("exists");
        not = getTestCommand().getParameterByPath("not");
        TestParameter elementExists = getTestCommand().getParameterByPath("elementExists");
        TestParameter elementVisible = getTestCommand().getParameterByPath("elementVisible");

        if (startElement.exists()) {
            SmartSpecifiedElement smartSpecifiedElement = smartSpecifiedElement();
            Element testElement = smartSpecifiedElement.getJSoupElement();

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
                    skipIfStatement(false);
                } else {  // FALSE
                    // As it was false we now need to skip all statements until the end
                    skipIfStatement(true);
                }
            }

            if (contains.exists()) {
                if ("".equals(valueToCheck)) { // FALSE
                    // If no element was found (so valueToCheck is "") then it does not contain our text, return false
                    skipIfStatement(true);
                } else {
                    if (valueToCheck.contains(contains.getParameterValue())) { // TRUE
                        skipIfStatement(false);
                    } else {  // FALSE
                        // As it was false we now need to skip all statements until the end
                        skipIfStatement(true);
                    }
                }
            }

            if (exists.exists()) { // Test to see if a specific parameter exists
                if (!"".equals(valueToCheck)) {  // TRUE
                    if (exists.getParameterValue().equals("false")) {
                        skipIfStatement(true);
                    }
                } else {  // FALSE
                    if (exists.getParameterValue().equals("true")) {
                        skipIfStatement(true);
                    }
                }
            }

            if (elementExists.exists()) { // Test to see if the element exists
                if (testElement != null) {  // TRUE
                    if (elementExists.getParameterValue().equals("false")) {
                        skipIfStatement(true);
                    }
                } else {  // FALSE
                    if (elementExists.getParameterValue().equals("true")) {
                        skipIfStatement(true);
                    }
                }
            }

            if (elementVisible.exists()) { // Test to see if the element is currently visible
                WebElement testWebElement = smartSpecifiedElement.getSeleniumElement();
                if (testElement != null && testWebElement != null && testWebElement.isDisplayed()) {  // TRUE
                    if (elementVisible.getParameterValue().equals("false")) {
                        skipIfStatement(true);
                    } else {
                        skipIfStatement(false);
                    }
                } else {  // FALSE
                    skipIfStatement(true);
                }
            }
        }
    }

    // Decide if we skip this if statement depending on not modifier
    private void skipIfStatement(Boolean skip) {
        if ((!isNot() && skip) || (isNot() && !skip)) {
            if (startElement != null && startElement.exists()) {
                getIfTracker().setIsSkippingIf(true);
                getIfTracker().setIfReference(startElement.getParameterValue());
            }
        }
    }

    // Tell us if there is a not within this command
    private Boolean isNot() {
        return not != null && not.exists();
    }
}
