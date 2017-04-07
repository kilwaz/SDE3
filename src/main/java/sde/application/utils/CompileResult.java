package sde.application.utils;

import java.util.ArrayList;
import java.util.List;

public class CompileResult {
    private String className;
    private Boolean successfulCompile;
    private List<CompiledException> compiledExceptions;
    private List<CompileLineError> compiledLineErrors;
    private List<CompileError> compiledError;

    public CompileResult() {
        compiledExceptions = new ArrayList<>();
        compiledLineErrors = new ArrayList<>();
        compiledError = new ArrayList<>();
        successfulCompile = false;
        className = null;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Boolean getSuccessfulCompile() {
        return successfulCompile;
    }

    public void setSuccessfulCompile(Boolean successfulCompile) {
        this.successfulCompile = successfulCompile;
    }

    public void addLineCompileError(Integer lineNumber, String errString) {
        while (lineAlreadyExists(lineNumber, errString)) {
            errString = errString + " ";
        }
        compiledLineErrors.add(new CompileLineError(lineNumber, errString));
    }

    // Used to check if the line already exists as in within the errors, this is a problem for ace editor if the same line exists within the same line number
    private Boolean lineAlreadyExists(Integer lineNumber, String errString) {
        for (CompileLineError compileLineError : compiledLineErrors) {
            if (lineNumber.equals(compileLineError.getLineNumber()) && compileLineError.getOutError().equals(errString)) {
                return true;
            }
        }
        return false;
    }

    public void addCompileError(String outString) {
        compiledError.add(new CompileError(outString));
    }

    public void addCompiledException(Exception exception) {
        compiledExceptions.add(new CompiledException(exception));
    }

    public List<CompileLineError> getCompiledLineErrors() {
        return compiledLineErrors;
    }
}
