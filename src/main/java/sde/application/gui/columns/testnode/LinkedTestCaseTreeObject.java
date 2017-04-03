package sde.application.gui.columns.testnode;

import sde.application.node.objects.LinkedTestCase;
import javafx.scene.control.TreeItem;

public class LinkedTestCaseTreeObject {
    public static final Integer UNKNOWN = -1;
    public static final Integer LINKED_TEST_ROOT = 1;
    public static final Integer LINKED_TEST_CASE = 2;

    private LinkedTestCase linkedTestCase;
    private TreeItem<LinkedTestCaseTreeObject> treeItem;

    private Integer type = UNKNOWN;

    private LinkedTestCaseTreeObject() {
        this.type = LINKED_TEST_ROOT;
    }

    private LinkedTestCaseTreeObject(LinkedTestCase linkedTestCase) {
        this.linkedTestCase = linkedTestCase;
        this.type = LINKED_TEST_CASE;
    }

    public static TreeItem<LinkedTestCaseTreeObject> createTreeItem() {
        LinkedTestCaseTreeObject linkedTestCaseTreeObject = new LinkedTestCaseTreeObject();
        TreeItem<LinkedTestCaseTreeObject> treeItem = new TreeItem<>(linkedTestCaseTreeObject);
        linkedTestCaseTreeObject.setTreeItem(treeItem);
        return treeItem;
    }

    public static TreeItem<LinkedTestCaseTreeObject> createTreeItem(LinkedTestCase linkedTestCase) {
        LinkedTestCaseTreeObject linkedTestCaseTreeObject = new LinkedTestCaseTreeObject(linkedTestCase);
        TreeItem<LinkedTestCaseTreeObject> treeItem = new TreeItem<>(linkedTestCaseTreeObject);
        linkedTestCaseTreeObject.setTreeItem(treeItem);
        return treeItem;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        if (type.equals(LINKED_TEST_ROOT)) {
            return "Tests";
        } else if (type.equals(LINKED_TEST_CASE)) {
            return linkedTestCase.getTestCaseNodeName();
        }

        return null;
    }

    public String isEnabledString() {
        if (type.equals(LINKED_TEST_CASE)) {
            return linkedTestCase.isEnabled().toString();
        }

        return "";
    }

    public Boolean isEnabled() {
        if (type.equals(LINKED_TEST_CASE)) {
            return linkedTestCase.isEnabled();
        }

        return null;
    }

    public TreeItem<LinkedTestCaseTreeObject> getTreeItem() {
        return treeItem;
    }

    public void setTreeItem(TreeItem<LinkedTestCaseTreeObject> treeItem) {
        this.treeItem = treeItem;
    }

    public LinkedTestCase getLinkedTestCase() {
        return linkedTestCase;
    }
}


