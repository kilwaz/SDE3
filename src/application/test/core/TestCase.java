package application.test.core;

import application.error.Error;
import application.node.design.DrawableNode;
import application.node.implementations.InputNode;
import application.node.implementations.TestNode;
import application.node.objects.Input;
import application.node.objects.Test;
import application.test.*;
import application.test.action.helpers.PageStateCapture;
import application.test.annotation.AssertChange;
import application.utils.SDEThread;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestCase<TemplateCase extends TestTemplate> {
    private static Logger log = Logger.getLogger(TestCase.class);
    private String testIterationID = "";
    private TestSet testSet;
    private Test test = null;
    private List<Input> inputs = new ArrayList<>();
    private List<String> testNodeList = new ArrayList<>();
    private List<String> inputNodeList = new ArrayList<>();
    private ExpectedElements expectedElements = new ExpectedElements();
    private ChangedElements changedElements = new ChangedElements();
    private Method buildMethod = null;
    private Method inputMethod = null;
    private Method runMethod = null;
    private Method onCompleteMethod = null;
    private Method threadWaitMethod = null;
    private PageStateCapture captureBefore = null;
    private PageStateCapture captureAfter = null;
    private HashMap<String, PageStateCapture> pageCaptures = new HashMap<>();
    private Class<TemplateCase> templateCaseClass;
    private TestTemplate templateObject;
    private ObservableList<TestCommand> testCommands = FXCollections.observableArrayList();

    public TestCase() {
    }

    public TestCase(Class<TemplateCase> testClass) {
        this.templateCaseClass = testClass;
    }

    public void addTestCommand(TestCommand testCommand) {
        testCommands.add(testCommand);
    }

    public ObservableList<TestCommand> getTestCommands() {
        return testCommands;
    }

    public TestCase templateObject(TestTemplate templateObject) {
        this.templateObject = templateObject;
        return this;
    }

    public TestCase testIterationID(String testIterationID) {
        this.testIterationID = testIterationID;
        return this;
    }

    public TestCase input(Input input) {
        List<Input> inputList = new ArrayList<>();
        inputList.add(input);
        return inputs(inputList);
    }

    public TestCase inputs(List<Input> inputs) {
        this.inputs.addAll(inputs);
        return this;
    }

    public TestCase templateCaseClass(Class<TemplateCase> templateCaseClass) {
        this.templateCaseClass = templateCaseClass;
        return this;
    }

    public TestCase buildMethod(Method buildMethod) {
        this.buildMethod = buildMethod;
        return this;
    }

    public TestCase init() {
        buildTest();
        runTest();
        compareTest();
        completeTest();
        threadWait();
        return this;
    }

    public TestCase testNodes(List<String> testNodeList) {
        this.testNodeList.addAll(testNodeList);
        return this;
    }

    public TestCase inputNodes(List<String> inputNodeList) {
        this.inputNodeList.addAll(inputNodeList);
        return this;
    }

    public TestCase parent(TestSet parentCase) {
        this.testSet = parentCase;
        return this;
    }

    public Test getTest() {
        return test;
    }

    private void threadWait() {
        try {
            if (threadWaitMethod != null) {
                threadWaitMethod.invoke(templateObject);
            }
        } catch (IllegalAccessException | InvocationTargetException ex) {
            Error.TEST_CASE_THREAD_WAIT_METHOD_NOT_FOUND.record().create(ex);
        }
    }

    private void buildTest() {
        try {
            // Parent cannot be null
            if (testSet.getParentNode() == null) {
                // Cannot continue with test if no program is assigned to this test case
                Error.TEST_CASE_NO_PROGRAM_SET.record().create();
                return;
            }

            // Build the tests first
            if (buildMethod != null) {
                buildMethod.invoke(templateObject, this);
            }
            for (String nodeName : testNodeList) {
                DrawableNode node = testSet.getParentNode().getProgram().getFlowController().getNodeThisControllerFromContainedText(nodeName);
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
                inputMethod.invoke(templateObject);
            }
            if (test != null) {
                for (String nodeName : inputNodeList) {
                    DrawableNode node = testSet.getParentNode().getProgram().getFlowController().getNodeThisControllerFromContainedText(nodeName);
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
        } catch (IllegalAccessException | InvocationTargetException ex) {
            Error.TEST_ANNOTATION_MISSING.record().create(ex);
        }
    }

    private void runTest() {
        // Run the test
        try {
            if (runMethod != null) {
                runMethod.invoke(templateObject);
            }
            if (test != null) {
                test.testCase(this);
                TestRunner testRunner = new TestRunner(test, testSet.getParentNode().getProgram());
                SDEThread sdeThread = new SDEThread(testRunner, "Test case for " + testSet.getParentNode().getUuidString(), null, true);
                sdeThread.join();
            }

        } catch (IllegalAccessException | InvocationTargetException ex) {
            Error.TEST_ANNOTATION_MISSING.record().create(ex);
        }
    }

    private void compareTest() {
        if (test != null && captureBefore != null && captureAfter != null) {
            // This needs to be done after the test has completed as some of the assertions require knowledge of elements from the test results
            for (Method method : templateCaseClass.getDeclaredMethods()) {
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

                    AssertData assertData = new AssertData();
                    assertData.expectedElement(expectedElement).inputs(inputs);
                    assertData.before(captureBefore).after(captureAfter);
                    assertData.states(pageCaptures);

                    try {
                        method.invoke(templateObject, assertData);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        Error.TEST_ANNOTATION_MISSING.record().create(ex);
                    }

                    expectedElements.add(expectedElement);
                }
            }

            // Finally compare the results
            changedElements = captureBefore.compare(captureAfter);
        }
    }

    private void completeTest() {
        try {
            // Once Test is complete
            if (onCompleteMethod != null) {
                OnCompleteData onCompleteData = new OnCompleteData();
                onCompleteData.testCase(this);
                onCompleteMethod.invoke(templateObject, onCompleteData);
            }
        } catch (IllegalAccessException | InvocationTargetException ex) {
            Error.TEST_ANNOTATION_MISSING.record().create(ex);
        }
    }

    public TestCase inputMethod(Method inputMethod) {
        this.inputMethod = inputMethod;
        return this;
    }

    public TestCase runMethod(Method runMethod) {
        this.runMethod = runMethod;
        return this;
    }

    public TestCase onCompleteMethod(Method onCompleteMethod) {
        this.onCompleteMethod = onCompleteMethod;
        return this;
    }

    public TestCase threadWaitMethod(Method threadWaitMethod) {
        this.threadWaitMethod = threadWaitMethod;
        return this;
    }

    public void setPageCaptures(PageStateCapture captureBefore, PageStateCapture captureAfter) {
        this.captureAfter = captureAfter;
        this.captureBefore = captureBefore;
    }

    public ExpectedElements getExpectedElements() {
        return expectedElements;
    }

    public ChangedElements getChangedElements() {
        return changedElements;
    }

    public PageStateCapture getCaptureAfter() {
        return captureAfter;
    }

    public void setCaptureAfter(PageStateCapture captureAfter) {
        this.captureAfter = captureAfter;
    }

    public PageStateCapture getCaptureBefore() {
        return captureBefore;
    }

    public void setCaptureBefore(PageStateCapture captureBefore) {
        this.captureBefore = captureBefore;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public Input getInput(String name) {
        if (name != null) {
            for (Input input : inputs) {
                if (name.equals(input.getVariableName())) {
                    return input;
                }
            }
        }

        return null;
    }

    // This is called from within the application test case template
    public TestSet getTestSet() {
        return testSet;
    }

    public String getTestIterationID() {
        return testIterationID;
    }

    public PageStateCapture getStoredPageState(String stateName) {
        return pageCaptures.get(stateName);
    }

    public void storePageState(String stateName, PageStateCapture capture) {
        pageCaptures.put(stateName, capture);
    }
}
