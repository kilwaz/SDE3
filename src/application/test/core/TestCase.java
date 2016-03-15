package application.test.core;

import application.error.Error;
import application.node.design.DrawableNode;
import application.node.implementations.InputNode;
import application.node.implementations.TestNode;
import application.node.objects.Input;
import application.node.objects.Test;
import application.test.ChangedElements;
import application.test.ExpectedElement;
import application.test.ExpectedElements;
import application.test.TestRunner;
import application.test.action.helpers.PageStateCapture;
import application.test.annotation.*;
import application.utils.SDEThread;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCase<TestClass extends TestCase> {
    private static Logger log = Logger.getLogger(TestCase.class);

    private List<String> testNodeList = new ArrayList<>();
    private List<String> inputNodeList = new ArrayList<>();
    private Class<TestClass> testClass;
    private DrawableNode parentNode = null;
    private List<Input> inputs = new ArrayList<>();
    private ExpectedElements expectedElements = new ExpectedElements();
    private ChangedElements changedElements = new ChangedElements();

    private PageStateCapture captureBefore = null;
    private PageStateCapture captureAfter = null;

    public TestCase(Class<TestClass> testClass) {
        this.testClass = testClass;
    }

    public TestCase init() {
        Method buildMethod = null;
        Method inputMethod = null;
        Method runMethod = null;
        Method onCompleteMethod = null;

        // Find any test nodes assigned to the test
        if (testClass.isAnnotationPresent(TestNodes.class)) {
            Annotation annotation = testClass.getAnnotation(TestNodes.class);
            String[] testList = ((TestNodes) annotation).testList();
            Collections.addAll(testNodeList, testList);
        }
        // Find any input nodes assigned to the test
        if (testClass.isAnnotationPresent(InputNodes.class)) {
            Annotation annotation = testClass.getAnnotation(InputNodes.class);
            String[] inputList = ((InputNodes) annotation).inputList();
            Collections.addAll(inputNodeList, inputList);
        }

        Annotation[] testInputAnnotations = testClass.getAnnotationsByType(TestInput.class);
        for (Annotation annotation : testInputAnnotations) {
            TestInput testInputAnnotation = (TestInput) annotation;
            Input input = Input.create(Input.class);
            input.setVariableName(testInputAnnotation.name());
            input.setVariableValue(testInputAnnotation.val());
            inputs.add(input);
        }

        // Annotations against methods
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(BuildTest.class)) { // During construction of the test commands to control the browser
                buildMethod = method;
            } else if (method.isAnnotationPresent(ApplyInputs.class)) { // Applying any inputs to the test script
                inputMethod = method;
            } else if (method.isAnnotationPresent(RunTest.class)) { // Running the test within selenium
                runMethod = method;
            } else if (method.isAnnotationPresent(OnComplete.class)) { // Once all tests have been completed
                onCompleteMethod = method;
            }
        }

        Test test = null;
        try {
            // Parent cannot be null
            if (parentNode == null) {
                // Cannot continue with test if no program is assigned to this test case
                Error.TEST_CASE_NO_PROGRAM_SET.record().create();
                return this;
            }

            // Build the tests first
            if (buildMethod != null) {
                buildMethod.invoke(this);
            }
            for (String nodeName : testNodeList) {
                DrawableNode node = parentNode.getProgram().getFlowController().getNodeThisControllerFromContainedText(nodeName);
                if (node == null) { // Check that the node exists first
                    Error.TEST_CASE_CAN_FIND_NODE.record().additionalInformation("Node name " + nodeName).create();
                } else {
                    if (node instanceof TestNode) { // Check the node is of the expected type
                        TestNode testNode = (TestNode) node;
                        if (test == null) {
                            test = testNode.cloneTest();
                        } else {
                            test.append(testNode.cloneTest());
                        }
                    } else {
                        Error.TEST_CASE_NODE_TYPE_MISMATCH.record().additionalInformation("Expected TestNode").create();
                    }
                }
            }

            // Add any inputs to the tests
            if (inputMethod != null) {
                inputMethod.invoke(this);
            }
            if (test != null) {
                for (String nodeName : inputNodeList) {
                    DrawableNode node = parentNode.getProgram().getFlowController().getNodeThisControllerFromContainedText(nodeName);
                    if (node == null) { // Check that the node exists first
                        Error.TEST_CASE_CAN_FIND_NODE.record().additionalInformation("Node name " + nodeName).create();
                    } else {
                        if (node instanceof InputNode) { // Check the node is of the expected type
                            InputNode inputNode = (InputNode) node;
                            test.applyInputs(inputNode);
                            inputs.addAll(inputNode.getInputs());
                        } else {
                            Error.TEST_CASE_NODE_TYPE_MISMATCH.record().additionalInformation("Expected InputNode").create();
                        }
                    }
                }
                for (Input input : inputs) {
                    test.applyInput(input);
                }
            }

            // Run the test
            if (runMethod != null) {
                runMethod.invoke(this);
            }
            if (test != null) {
                test.testCase(this);
                TestRunner testRunner = new TestRunner(test, parentNode.getProgram());
                SDEThread sdeThread = new SDEThread(testRunner, "Test case for " + parentNode.getUuidString());
                sdeThread.join();
            }

            // Compare and process the results
            if (test != null) {
                // This needs to be done after the test has completed as some of the assertions require knowledge of elements from the test results
                for (Method method : testClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(AssertChange.class)) {
                        Annotation annotation = method.getAnnotation(AssertChange.class);
                        AssertChange assertChange = (AssertChange) annotation;
                        ExpectedElement expectedElement = ExpectedElement.define();

                        // id
                        if (!assertChange.id().isEmpty()) {
                            expectedElement.id(assertChange.id());
                        }

                        // type
                        if (!assertChange.type().isEmpty()) {
                            expectedElement.type(assertChange.type());
                        }

                        // attribute
                        if (!assertChange.attribute().isEmpty()) {
                            expectedElement.attribute(assertChange.attribute());
                        }

                        // before
                        if (!assertChange.before().isEmpty()) {
                            expectedElement.before(assertChange.before());
                        }

                        // after
                        if (!assertChange.after().isEmpty()) {
                            expectedElement.after(assertChange.after());
                        }

                        // increasedBy
                        if (!assertChange.increasedBy().isEmpty()) {
                            expectedElement.increasedBy(assertChange.increasedBy());
                        }

                        try {
                            method.invoke(this, expectedElement);
                        } catch (IllegalAccessException | InvocationTargetException ex) {
                            Error.TEST_ANNOTATION_MISSING.record().create(ex);
                        }

                        expectedElements.add(expectedElement);
                    }
                }

                // Finally compare the results
                changedElements = captureBefore.compare(captureAfter);
            }

            // Once Test is complete
            if (onCompleteMethod != null) {
                onCompleteMethod.invoke(this);
            }
        } catch (IllegalAccessException | InvocationTargetException ex) {
            Error.TEST_ANNOTATION_MISSING.record().create(ex);
        }

        return this;
    }

    public DrawableNode getParentNode() {
        return parentNode;
    }

    public TestCase setParentNode(DrawableNode parentNode) {
        this.parentNode = parentNode;
        return this;
    }

    public List<String> getTestNodeList() {
        return testNodeList;
    }

    public List<String> getInputNodeList() {
        return inputNodeList;
    }

    public ExpectedElements getExpectedElements() {
        return expectedElements;
    }

    public ChangedElements getChangedElements() {
        return changedElements;
    }

    public String testInput(String name) {
        for (Input input : inputs) {
            if (input.getVariableName().equals(name)) {
                return input.getVariableValue();
            }
        }

        return null;
    }

    public PageStateCapture getCaptureAfter() {
        return captureAfter;
    }

    public PageStateCapture getCaptureBefore() {
        return captureBefore;
    }

    public void setCaptureAfter(PageStateCapture captureAfter) {
        this.captureAfter = captureAfter;
    }

    public void setPageCaptures(PageStateCapture captureBefore, PageStateCapture captureAfter) {
        this.captureAfter = captureAfter;
        this.captureBefore = captureBefore;
    }

    public void setCaptureBefore(PageStateCapture captureBefore) {
        this.captureBefore = captureBefore;
    }
}
