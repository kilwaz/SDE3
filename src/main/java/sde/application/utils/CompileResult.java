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
        compiledLineErrors.add(new CompileLineError(lineNumber, errString));
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
