package application.node.objects;

import application.node.implementations.TestNode;

public class Test {
    private TestNode parentTestNode;
    private String text = "";
    private Integer currentLine = 0;
    private Boolean continueTest = true;

    public Test(TestNode testNode) {
        this.parentTestNode = testNode;
        this.text = "";
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            if (!parentTestNode.isInitialising()) {
                parentTestNode.save();
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
}
