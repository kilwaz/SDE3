package application.test.action;

import application.data.DataBank;
import application.node.design.DrawableNode;
import application.node.implementations.TestResultNode;
import application.test.TestParameter;
import application.test.TestStep;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 */
public class TestWebAction extends WebAction {
    public TestWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter xPathElement = getTestCommand().getParameterByPath("element::xPath");
        By testBy = null;


        if (xPathElement != null) {
            testBy = findElement(xPathElement);
        }

        if (testBy != null) {
            WebElement testElement = getDriver().findElement(testBy);

            if (getTestCommand().getParameterByPath("equals") != null) {
                TestStep testStep = DataBank.createNewTestStep(getTestResult());
                testStep.setTestType(TestStep.TEST_TYPE_EQUAL);
                getTestResult().addTestStep(testStep);
                testStep.setTestString(getTestCommand().getRawCommand());

                String contentToEqual = getTestCommand().getParameterByPath("equals").getParameterValue();
                String content = testElement.getText().trim();

                takeScreenshotOfElement(testStep, testElement);

                if (contentToEqual.equals(content)) {
                    testStep.setSuccessful(true);
                } else {
                    testStep.setSuccessful(false);
                }
                testStep.setExpectedEqual(contentToEqual);
                testStep.setObservedEqual(content);

                DataBank.saveTestStep(testStep);

                // Add the result to a result node if it is linked
                TestParameter resultNodeName = getTestCommand().getParameterByPath("resultNode");
                if (resultNodeName != null) {
                    DrawableNode resultNode = DataBank.currentlyEditProgram.getFlowController().getNodeThisControllerFromContainedText(resultNodeName.getParameterValue());
                    if (resultNode != null && resultNode instanceof TestResultNode) {
                        TestResultNode testResultNode = (TestResultNode) resultNode;
                        testResultNode.addResult(testStep);
                    }
                }
            }
        }
    }
}
