package application.test;

import application.data.model.DatabaseObject;

import java.util.ArrayList;
import java.util.List;

public class TestResult extends DatabaseObject {
    private List<TestStep> testSteps = new ArrayList<>();

    public TestResult() {
        super();
    }

    public void addTestStep(TestStep testStep) {
        testSteps.add(testStep);
        testStep.setParentResult(this);
    }

    public List<TestStep> getTestSteps() {
        return testSteps;
    }
}
