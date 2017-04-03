package sde.application.utils;

public class CompiledException {
    private Exception exception;

    public CompiledException(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
