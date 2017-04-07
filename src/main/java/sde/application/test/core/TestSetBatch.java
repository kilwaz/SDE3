package sde.application.test.core;

import sde.application.data.model.DatabaseObject;
import sde.application.node.implementations.TestManagerNode;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class TestSetBatch extends DatabaseObject {
    private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("kk:mm:ss dd MMM yyyy");
    private List<TestSet> testSets = new ArrayList<>();
    private ObservableList<TestCase> testCases = FXCollections.observableArrayList();
    private DateTime createdTime;
    private SimpleIntegerProperty caseCount;
    private TestManagerNode parentNode;

    public TestSetBatch() {
        super();

        createdTime = new DateTime();
        caseCount = new SimpleIntegerProperty();
        caseCount.bind(Bindings.size(testCases));
    }

    public TestManagerNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(TestManagerNode parentNode) {
        this.parentNode = parentNode;
    }

    public String getParentUuid() {
        if (parentNode != null) {
            return parentNode.getUuidString();
        }
        return null;
    }

    public void addTestSet(TestSet testSet) {
        testSets.add(testSet);
    }

    public void addTestCase(TestCase testCase) {
        testCases.add(testCase);
    }

    public List<TestSet> getTestSets() {
        return testSets;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public void setParent(TestManagerNode parentNode) {
        this.parentNode = parentNode;
    }

    public String getFormattedTime() {
        return dateFormatter.print(createdTime);
    }

    public int getCaseCount() {
        return caseCount.get();
    }

    public SimpleIntegerProperty caseCountProperty() {
        return caseCount;
    }
}
