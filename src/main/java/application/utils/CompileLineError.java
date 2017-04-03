package application.utils;

public class CompileLineError {
    private Integer lineNumber;
    private String outError;

    public CompileLineError(Integer lineNumber, String outError) {
        this.lineNumber = lineNumber;
        this.outError = outError;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public String getOutError() {
        return outError;
    }
}
