package sde.application.utils;

public class CompileError {
    private String errString;

    public CompileError(String errString) {
        this.errString = errString;
    }

    public String getErrString() {
        return errString;
    }
}
