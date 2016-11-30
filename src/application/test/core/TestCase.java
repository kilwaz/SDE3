package application.test.core;

import application.data.model.DatabaseObject;
import application.error.Error;
import application.net.proxy.ProxyRequestListener;
import application.net.proxy.RecordedRequest;
import application.node.design.DrawableNode;
import application.node.implementations.InputNode;
import application.node.implementations.TestNode;
import application.node.objects.Input;
import application.node.objects.Test;
import application.test.PageStateCompare;
import application.test.TestCommand;
import application.test.TestLogMessage;
import application.test.TestRunner;
import application.test.action.helpers.PageStateCapture;
import application.utils.SDEThread;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestCase<TemplateCase extends TestTemplate> extends DatabaseObject implements ProxyRequestListener {
    private static Logger log = Logger.getLogger(TestCase.class);
    private String testIterationID = "";
    private TestSet testSet;
    private Test test = null;
    private List<Input> inputs = new ArrayList<>();
    private List<String> testNodeList = new ArrayList<>();
    private List<String> inputNodeList = new ArrayList<>();
    private Method buildMethod = null;
    private Method inputMethod = null;
    private Method runMethod = null;
    private Method onCompleteMethod = null;
    private Method threadWaitMethod = null;
    private HashMap<String, PageStateCapture> pageCaptures = new HashMap<>();
    private ObservableList<PageStateCompare> pageCapturesCompares = FXCollections.observableArrayList();
    private Class<TemplateCase> templateCaseClass;
    private TestTemplate templateObject;
    private ObservableList<TestCommand> testCommands = FXCollections.observableArrayList();
    private ObservableList<RecordedRequest> testRequests = FXCollections.observableArrayList();
    private SimpleIntegerProperty passedTestCases = new SimpleIntegerProperty();

    public TestCase() {
        passedTestCases.set(0);
    }

    public TestCase(Class<TemplateCase> testClass) {
        this.templateCaseClass = testClass;
    }

    public int getPassedTestCases() {
        return passedTestCases.get();
    }

    public SimpleIntegerProperty passedTestCasesProperty() {
        return passedTestCases;
    }

    public void addTestCommand(TestCommand testCommand) {
        testCommand.setParentTestCase(this);
        testCommand.save();
        testCommands.add(testCommand);
    }

    public ObservableList<TestCommand> getTestCommands() {
        return testCommands;
    }

    public ObservableList<RecordedRequest> getTestRequests() {
        return testRequests;
    }

    public String getLogMessages() {
        StringBuilder logMessage = new StringBuilder();
//        for (TestLogMessage testLogMessage : logMessages) {
//            logMessage.append(testLogMessage.getFormattedDateTime()).append(": ").append(testLogMessage.getMessage()).append("\n");
//        }
        return logMessage.toString();
    }

    public TestCase log(Exception ex) {
        TestLogMessage testLogMessage = new TestLogMessage();
        testLogMessage.setMessage(ex.toString());
        testLogMessage.setParentTestCase(this);
        testLogMessage.save();
        return this;
    }

    public TestCase log(String logMessage) {
        TestLogMessage testLogMessage = new TestLogMessage();
        testLogMessage.setMessage(logMessage);
        testLogMessage.setParentTestCase(this);
        testLogMessage.save();
        return this;
    }

    public TestTemplate getTemplateObject() {
        return templateObject;
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

    public Class<TemplateCase> getTemplateCaseClass() {
        return templateCaseClass;
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
        // Trigger the test comparisons
        pageCapturesCompares.forEach(PageStateCompare::compareTest);
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
        log.info("Added new page capture start");
        pageCapturesCompares.add(new PageStateCompare(this, captureBefore, captureAfter));
        log.info("Added new page capture end");
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

    public void storePageState(PageStateCapture capture) {
        pageCaptures.put(capture.getStateName(), capture);
    }

    @Override
    public void addRequest(RecordedRequest recordedRequest) {
//        testRequests.add(recordedRequest);
//        recordedRequest.setParentTestCase(this);
//        recordedRequest.save();
//        recordedRequest.lighten();
    }

    public HashMap<String, PageStateCapture> getPageCaptures() {
        return pageCaptures;
    }

    public ObservableList<PageStateCompare> getPageCapturesCompares() {
        return pageCapturesCompares;
    }
}
