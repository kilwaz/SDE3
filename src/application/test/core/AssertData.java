package application.test.core;

import application.node.objects.Input;
import application.test.ExpectedElement;
import application.test.action.helpers.PageStateCapture;

import java.util.ArrayList;
import java.util.List;

public class AssertData {
    private ExpectedElement expectedElement = null;
    private List<Input> inputs = new ArrayList<>();

    private PageStateCapture captureBefore = null;
    private PageStateCapture captureAfter = null;

    public AssertData() {

    }

    public String input(String name) {
        for (Input input : inputs) {
            if (input.getVariableName().equals(name)) {
                return input.getVariableValue();
            }
        }

        return null;
    }

    public AssertData inputs(List<Input> inputs) {
        this.inputs = inputs;
        return this;
    }

    public ExpectedElement expectedElement() {
        return expectedElement;
    }

    public AssertData expectedElement(ExpectedElement expectedElement) {
        this.expectedElement = expectedElement;
        return this;
    }

    public PageStateCapture before() {
        return captureBefore;
    }

    public AssertData before(PageStateCapture captureBefore) {
        this.captureBefore = captureBefore;
        return this;
    }

    public PageStateCapture after() {
        return captureAfter;
    }

    public AssertData after(PageStateCapture captureAfter) {
        this.captureAfter = captureAfter;
        return this;
    }
}
