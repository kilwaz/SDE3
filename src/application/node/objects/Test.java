package application.node.objects;

import application.node.implementations.InputNode;
import application.node.implementations.TestNode;
import application.test.core.TestCase;

import java.util.regex.Pattern;

public class Test {
    private TestNode parentTestNode;
    private String text = "";
    private Integer currentLine = 0;
    private Boolean continueTest = true;
    private Boolean clone = false;
    private TestCase testCase = null;

    public Test(TestNode testNode) {
        this.parentTestNode = testNode;
        this.text = "";
    }

    public Test() {
        this.text = "";
        clone = true;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (clone) {
            if (!this.text.equals(text)) {
                this.text = text;
            }
        } else {
            if (!this.text.equals(text)) {
                this.text = text;
                if (!parentTestNode.isInitialising()) {
                    parentTestNode.save();
                }
            }
        }
    }

    public void incrementLineNumber() {
        currentLine++;
    }

    public Integer getCurrentLine() {
        return currentLine;
    }

    public void setCurrentLine(Integer currentLine) {
        this.currentLine = currentLine;
    }

    public Boolean getContinueTest() {
        return continueTest;
    }

    public void setContinueTest(Boolean continueTest) {
        this.continueTest = continueTest;
    }

    public Test prefix(Test test) {
        text = test.getText() + text + System.lineSeparator();
        return this;
    }

    public Test append(Test test) {
        text += System.lineSeparator() + test.getText();
        return this;
    }

    public Test cloneTest() {
        Test cloneTest = new Test();
        cloneTest.setText(text);
        return cloneTest;
    }

    public Test applyInput(String name, String value) {
        if (name != null && !name.isEmpty() && value != null && !value.isEmpty()) {
            setText(getText().replaceAll(Pattern.quote(name), value.replace("$", "\\$")));
        }
        return this;
    }

    public Test applyInput(Input input) {
        applyInput(input.getVariableName(), input.getVariableValue());
        return this;
    }

    public Test applyInputs(InputNode inputNode) {
        inputNode.getInputs().forEach(this::applyInput);

        return this;
    }

    public Test testCase(TestCase testCase) {
        this.testCase = testCase;
        return this;
    }

    public TestCase getTestCase() {
        return testCase;
    }
}
