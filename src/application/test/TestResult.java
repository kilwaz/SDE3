package application.test;

import java.util.ArrayList;
import java.util.List;

public class TestResult {
    private Integer id = -1;
    private List<TestStep> testSteps = new ArrayList<>();

    public void addTestStep(TestStep testStep) {
        testSteps.add(testStep);
        testStep.setParentResult(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<TestStep> getTestSteps() {
        return testSteps;
    }
}
