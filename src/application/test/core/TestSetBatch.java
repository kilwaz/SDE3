package application.test.core;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class TestSetBatch {
    private List<TestSet> testSets = new ArrayList<>();
    private ObservableList<TestCase> testCases = FXCollections.observableArrayList();
    private DateTime created;
    private SimpleIntegerProperty caseCount;

    public TestSetBatch() {
        created = new DateTime();
        caseCount = new SimpleIntegerProperty();
        caseCount.bind(Bindings.size(testCases));
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
        return created;
    }

    public int getCaseCount() {
        return caseCount.get();
    }

    public SimpleIntegerProperty caseCountProperty() {
        return caseCount;
    }
}
