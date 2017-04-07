package sde.application.test.core;

public class OnCompleteData {
    private TestCase testCase = null;

    public OnCompleteData() {

    }

    public OnCompleteData testCase(TestCase testCase) {
        this.testCase = testCase;
        return this;
    }

    public TestCase testCase() {
        return testCase;
    }
}
