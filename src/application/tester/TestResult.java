package application.tester;

import org.joda.time.Duration;

public class TestResult {
    private String outcome = "-";
    private String expected = "-";
    private Duration duration;
    private PageStateCapture initialState;
    private PageStateCapture finalState;
    private ChangedElements changedElements;

    public TestResult() {
    }

    public String getOutcome() {
        return this.outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getExpected() {
        return this.expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public String getDuration() {
        if (duration == null) {
            return "-";
        } else {
            return this.duration.getMillis() + "ms";
        }
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public PageStateCapture getInitialState() {
        return this.initialState;
    }

    public void setInitialState(PageStateCapture initialState) {
        this.initialState = initialState;
    }

    public PageStateCapture getFinalState() {
        return this.finalState;
    }

    public void setFinalState(PageStateCapture finalState) {
        this.finalState = finalState;
    }

    public ChangedElements getChangedElements() {
        return this.changedElements;
    }

    public void setChangedElements(ChangedElements changedElements) {
        this.changedElements = changedElements;
    }
}
