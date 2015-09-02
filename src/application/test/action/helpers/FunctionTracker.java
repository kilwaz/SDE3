package application.test.action.helpers;

import java.util.HashMap;
import java.util.Stack;

public class FunctionTracker {
    private static HashMap<String, Integer> functionLineNumbers = new HashMap<>();
    private static Stack<Integer> functionStack = new Stack<>();
    private static String functionReference = "";
    private static Boolean isSkippingFunction = false;

    public static void cleanFunctions() {
        functionLineNumbers.clear();
        functionStack.clear();
    }

    public static void addFunction(String functionName, Integer lineNumber) {
        functionLineNumbers.put(functionName, lineNumber);
    }

    public static Integer getFunction(String functionName) {
        return functionLineNumbers.get(functionName);
    }

    public static void pushFunctionStack(Integer lineNumber) {
        functionStack.push(lineNumber);
    }

    public static Integer popFunctionStack() {
        return functionStack.pop();
    }

    public static String getFunctionReference() {
        return functionReference;
    }

    public static void setFunctionReference(String functionReference) {
        FunctionTracker.functionReference = functionReference;
    }

    public static Boolean isSkippingFunction() {
        return isSkippingFunction;
    }

    public static void setIsSkippingFunction(Boolean isSkippingFunction) {
        FunctionTracker.isSkippingFunction = isSkippingFunction;
    }
}
