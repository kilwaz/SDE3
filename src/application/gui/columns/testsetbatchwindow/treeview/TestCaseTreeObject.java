package application.gui.columns.testsetbatchwindow.treeview;

import application.test.core.TestCase;
import application.test.core.TestSet;
import javafx.scene.control.TreeItem;

public class TestCaseTreeObject {
    public static final Integer UNKNOWN = -1;
    public static final Integer TEST_ROOT = 1;
    public static final Integer TEST_SET = 2;
    public static final Integer TEST_CASE = 3;
    private TestCase testCase;
    private TestSet testSet;
    private Integer type = UNKNOWN;

    private TestCaseTreeObject() {
        this.type = TEST_ROOT;
    }

    private TestCaseTreeObject(TestSet testSet) {
        this.testSet = testSet;
        this.type = TEST_SET;
    }

    private TestCaseTreeObject(TestCase testCase) {
        this.testCase = testCase;
        this.type = TEST_CASE;
    }

    public static TreeItem<TestCaseTreeObject> createTreeItem() {
        return new TreeItem<>(new TestCaseTreeObject());
    }

    public static TreeItem<TestCaseTreeObject> createTreeItem(TestSet testSet) {
        return new TreeItem<>(new TestCaseTreeObject(testSet));
    }

    public static TreeItem<TestCaseTreeObject> createTreeItem(TestCase testCase) {
        return new TreeItem<>(new TestCaseTreeObject(testCase));
    }

    public Integer getType() {
        return type;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public TestSet getTestSet() {
        return testSet;
    }

    public String getName() {
        if (testSet != null) {  // Root for test set group
            return "Case " + testSet.getTestID();
        } else if (testCase != null) {// Test case provided
            return "Iteration " + testCase.getTestIterationID();
        } else {
            return "Tests";
        }
    }
}
