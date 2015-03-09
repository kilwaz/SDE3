package application.test;

import org.joda.time.Duration;

public class TestResultOld {
    private String outcome = "-";
    private String expected = "-";
    private Duration duration;
    private PageStateCapture initialState;
    private PageStateCapture finalState;
    private ChangedElements changedElements;

    public TestResultOld() {
    }

    public TestResultOld(TestResultOld testResultOld) {
        this.outcome = testResultOld.getOutcome();
        this.expected = testResultOld.getExpected();
        this.duration = new Duration(testResultOld.getDuration().getMillis());
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

    public Duration getDuration() {
        return this.duration;
    }

    public String getFormattedDuration() {
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