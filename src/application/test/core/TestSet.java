package application.test.core;

import application.node.design.DrawableNode;
import application.node.objects.Input;
import application.test.annotation.*;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestSet<Template extends TestTemplate> {
    private static Logger log = Logger.getLogger(TestSet.class);

    private DrawableNode parentNode = null;
    private String testID = "";
    private String testDescription = "";
    private Class templateTestClass;
    private TestTemplate templateObject;
    private List<TestCase> testCases = new ArrayList<>();
    private List<Input> additionalInputs = new ArrayList<>();

    public TestSet(TestTemplate templateObject) {
        this.templateObject = templateObject;
        templateTestClass = templateObject.getClass();
    }

    public TestSet additionalInput(Input input) {
        additionalInputs.add(input);
        return this;
    }

    public TestSet testID(String testID) {
        this.testID = testID;
        return this;
    }

    public TestSet init() {
        InputCombinations inputCombinations = getInputCombinations();

        Integer testIteration = 1;
        for (InputCombination inputCombination : inputCombinations.getInputCombinationList()) {
            TestCase testCase = createTestCaseWithInputCombination(inputCombination.getInputs());
            testCase.testIterationID(testIteration.toString());
            Input testIterationInput = Input.create(Input.class);
            testIterationInput.setVariableName("[[testIteration]]");
            testIterationInput.setVariableValue(testIteration.toString());
            testCase.input(testIterationInput);
            testCases.add(testCase);
            testIteration++;
        }

        testCases.forEach(TestCase::init);

        return this;
    }

    private TestCase createTestCaseWithInputCombination(List<Input> inputs) {
        TestCase testCase = new TestCase();
        testCase.templateCaseClass(templateTestClass);
        testCase.templateObject(templateObject);
        testCase.parent(this);
        testCase.input(getTestIDInput()).input(getTestDescriptionInput());
        testCase.inputs(inputs);
        testCase.testNodes(getTestNodes());
        testCase.inputNodes(getInputNodes());

        // Annotations against methods
        for (Method method : templateTestClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(BuildTest.class)) { // During construction of the test commands to control the browser
                testCase.buildMethod(method);
            } else if (method.isAnnotationPresent(ApplyInputs.class)) { // Applying any inputs to the test script
                testCase.inputMethod(method);
            } else if (method.isAnnotationPresent(RunTest.class)) { // Running the test within selenium
                testCase.runMethod(method);
            } else if (method.isAnnotationPresent(OnComplete.class)) { // Once all tests have been completed
                testCase.onCompleteMethod(method);
            } else if ("threadWait".equals(method.getName())) {
                testCase.threadWaitMethod(method);
            }
        }

        return testCase;
    }

    private InputCombinations getInputCombinations() {
        InputCombiner inputCombiner = new InputCombiner();

        Annotation[] testInputAnnotations = templateTestClass.getAnnotationsByType(TestInput.class);
        for (Annotation annotation : testInputAnnotations) {
            TestInput testInputAnnotation = (TestInput) annotation;
            TestInputList testInputList;
            if (testInputAnnotation.val().isEmpty()) {
                testInputList = new TestInputList(testInputAnnotation.name(), testInputAnnotation.list());
            } else {
                testInputList = new TestInputList(testInputAnnotation.name(), testInputAnnotation.val());
            }
            inputCombiner.addTestInput(testInputList);
        }

        // Any additional inputs which have been injected with node run params are handled here and added before being combined
        for (Input input : additionalInputs) {
            inputCombiner.addTestInput(new TestInputList(input.getVariableName(), input.getVariableValue()));
        }

        return inputCombiner.combine();
    }

    public DrawableNode getParentNode() {
        return parentNode;
    }

    public TestSet setParentNode(DrawableNode parentNode) {
        this.parentNode = parentNode;
        return this;
    }

    public String getTestID() {
        return testID;
    }

    public String getTestDescription() {
        return testDescription;
    }

    private Input getTestDescriptionInput() {
        Input testDescriptionInput = null;
        // Set the Test Description if it is presented
        if (templateTestClass.isAnnotationPresent(TestDescription.class)) {
            Annotation annotation = templateTestClass.getAnnotation(TestDescription.class);
            TestDescription testDescriptionAnnotation = ((TestDescription) annotation);
            testDescription = testDescriptionAnnotation.val();
            testDescriptionInput = Input.create(Input.class);
            testDescriptionInput.setVariableName(testDescriptionAnnotation.name());
            testDescriptionInput.setVariableValue(testDescriptionAnnotation.val());
        }
        return testDescriptionInput;
    }

    private Input getTestIDInput() {
        Input testIDInput = null;
        // Set the Test ID if it is presented
        if (templateTestClass.isAnnotationPresent(TestID.class)) {
            Annotation annotation = templateTestClass.getAnnotation(TestID.class);
            TestID testIDAnnotation = ((TestID) annotation);
            testID = testIDAnnotation.val();
            testIDInput = Input.create(Input.class);
            testIDInput.setVariableName(testIDAnnotation.name());
            testIDInput.setVariableValue(testIDAnnotation.val());
        } else if (testID != null) { // This can be set via nodeRunParams or if it is already set elsewhere
            log.info("TEST ID already set = " + testID);
            testIDInput = Input.create(Input.class);
            testIDInput.setVariableName("[[testID]]");
            testIDInput.setVariableValue(testID);
        }
        return testIDInput;
    }

    private List<String> getInputNodes() {
        List<String> inputNodeList = new ArrayList<>();
        // Find any input nodes assigned to the test
        if (templateTestClass.isAnnotationPresent(InputNodes.class)) {
            Annotation annotation = templateTestClass.getAnnotation(InputNodes.class);
            String[] inputList = ((InputNodes) annotation).inputList();
            Collections.addAll(inputNodeList, inputList);
        }
        return inputNodeList;
    }

    private List<String> getTestNodes() {
        List<String> testNodeList = new ArrayList<>();
        // Find any test nodes assigned to the test
        if (templateTestClass.isAnnotationPresent(TestNodes.class)) {
            Annotation annotation = templateTestClass.getAnnotation(TestNodes.class);
            String[] testList = ((TestNodes) annotation).testList();
            Collections.addAll(testNodeList, testList);
        }
        return testNodeList;
    }

    private List<Input> getTestInputs() {
        List<Input> testInputs = new ArrayList<>();

        Annotation[] testInputAnnotations = templateTestClass.getAnnotationsByType(TestInput.class);
        for (Annotation annotation : testInputAnnotations) {
            TestInput testInputAnnotation = (TestInput) annotation;
            Input testInput = Input.create(Input.class);
            testInput.setVariableName(testInputAnnotation.name());
            testInput.setVariableValue(testInputAnnotation.val());
            testInputs.add(testInput);
        }

        return testInputs;
    }
}
