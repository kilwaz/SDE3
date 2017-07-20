package sde.application.node.objects;

import sde.application.data.model.DatabaseObject;
import sde.application.node.implementations.InputNode;
import sde.application.node.implementations.TestNode;
import sde.application.test.core.TestCase;
import sde.application.test.core.TestStructure;

import java.io.InputStream;
import java.util.regex.Pattern;

public class Test extends DatabaseObject {
    private TestNode parentTestNode;
    private String text = "";
    private TestStructure testStructure;
    private Integer currentLine = 0;
    private Boolean continueTest = true;
    private Boolean clone = false;
    private TestCase testCase = null;
    private String fileOutputPath = null;
    private String fileRecordingPath = null;
    private String webDriverId = null;
    private Object recording = null;

    public Test() {
        super();
    }

    public Test(TestNode testNode) {
        this.parentTestNode = testNode;
        this.text = "";
        this.testStructure = new TestStructure();
    }

    public void setClone(Boolean clone) {
        this.clone = clone;
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
                if (parentTestNode != null && !parentTestNode.isInitialising()) {
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
        Test cloneTest = Test.create(Test.class);
        cloneTest.setParentTestNode(parentTestNode);
        cloneTest.setText(text);
        cloneTest.setClone(true);
        cloneTest.save();
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
        if (testCase == null) {
            testCase = new TestCase();
        }
        return testCase;
    }

    public void setParentTestNode(TestNode parentTestNode) {
        this.parentTestNode = parentTestNode;
    }

    public String getParentUuid() {
        if (parentTestNode != null) {
            return parentTestNode.getUuidString();
        }
        return null;
    }

    public String getFileOutputPath() {
        return fileOutputPath;
    }

    public void setFileOutputPath(String fileOutputPath) {
        this.fileOutputPath = fileOutputPath;
    }

    public String getFileRecordingPath() {
        return fileRecordingPath;
    }

    public void setFileRecordingPath(String fileRecordingPath) {
        this.fileRecordingPath = fileRecordingPath;
    }

    public String getWebDriverId() {
        return webDriverId;
    }

    public void setWebDriverId(String webDriverId) {
        this.webDriverId = webDriverId;
    }

    public InputStream getRecordingFileInputStream() {
        return null;
    }

    public void setRecordingFile(Object recordingFile) {
        // Probably don't want this to be serialised
        //recording = Serializer.deserialize(inputStream);
    }
}
