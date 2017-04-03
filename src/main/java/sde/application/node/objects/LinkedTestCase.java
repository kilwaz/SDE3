package sde.application.node.objects;

import sde.application.data.model.DatabaseObject;
import sde.application.data.model.dao.LinkedTestCaseDAO;
import sde.application.node.implementations.TestCaseNode;
import sde.application.node.implementations.TestManagerNode;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.List;

public class LinkedTestCase extends DatabaseObject {
    private TestCaseNode testCaseNode;
    private TestManagerNode testManagerNode;
    private SimpleBooleanProperty enabled = new SimpleBooleanProperty(false); // Default value of false
    private String type;
    private LinkedTestCase parentTestCase;
    private List<LinkedTestCase> childTestCases;

    public LinkedTestCase() {
    }

    public TestCaseNode getTestCaseNode() {
        return testCaseNode;
    }

    public void setTestCaseNode(TestCaseNode testCaseNode) {
        this.testCaseNode = testCaseNode;
    }

    public String getTestCaseNodeName() {
        if (testCaseNode != null) {
            return testCaseNode.getContainedText();
        } else {
            return "No test case";
        }
    }

    public String getTestCaseNodeUuid() {
        if (testCaseNode == null) {
            return null;
        } else {
            return testCaseNode.getUuidString();
        }
    }

    public SimpleBooleanProperty getEnabledProperty() {
        return enabled;
    }

    public Boolean getIsEnabled() {
        return enabled.get();
    }

    public Boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(Boolean enabled) {
        this.enabled.set(enabled);
    }

    public void setTestManagerNode(TestManagerNode testManagerNode) {
        this.testManagerNode = testManagerNode;
    }

    public String getTestManagerNodeUuid() {
        if (testManagerNode == null) {
            return null;
        } else {
            return testManagerNode.getUuidString();
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParentTestCaseUUID() {
        if (parentTestCase != null) {
            return parentTestCase.getUuidString();
        }
        return null;
    }

    public LinkedTestCase getParentTestCase() {
        return parentTestCase;
    }

    public void setParentTestCase(LinkedTestCase parentTestCase) {
        this.parentTestCase = parentTestCase;
    }

    public void addChildTestCase(LinkedTestCase linkedTestCase) {
        getChildTestCases().add(linkedTestCase);
        linkedTestCase.setParentTestCase(this);
        linkedTestCase.save();
    }

    public List<LinkedTestCase> getChildTestCases() {
        if (childTestCases == null) {
            LinkedTestCaseDAO linkedTestCaseDAO = new LinkedTestCaseDAO();
            childTestCases = linkedTestCaseDAO.getChildLinkedTestCases(this);
        }
        return childTestCases;
    }
}
