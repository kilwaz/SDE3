package application.test;

public class TestCommandError {
    private Exception exception = null;

    public TestCommandError() {

    }

    public Boolean hasException() {
        return exception != null;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
