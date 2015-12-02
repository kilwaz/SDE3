package application.test.action.helpers;

import java.util.HashMap;
import java.util.Stack;

public class FunctionTracker {
    private HashMap<String, Integer> functionLineNumbers = new HashMap<>();
    private Stack<Integer> functionStack = new Stack<>();
    private String functionReference = "";
    private Boolean isSkippingFunction = false;

    public void cleanFunctions() {
        functionLineNumbers.clear();
        functionStack.clear();
    }

    public void addFunction(String functionName, Integer lineNumber) {
        functionLineNumbers.put(functionName, lineNumber);
    }

    public Integer getFunction(String functionName) {
        return functionLineNumbers.get(functionName);
    }

    public void pushFunctionStack(Integer lineNumber) {
        functionStack.push(lineNumber);
    }

    public Integer popFunctionStack() {
        return functionStack.pop();
    }

    public String getFunctionReference() {
        return functionReference;
    }

    public void setFunctionReference(String functionReference) {
        this.functionReference = functionReference;
    }

    public Boolean isSkippingFunction() {
        return isSkippingFunction;
    }

    public void setIsSkippingFunction(Boolean isSkippingFunction) {
        this.isSkippingFunction = isSkippingFunction;
    }
}
