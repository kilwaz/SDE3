package application.tester;

import application.DrawableNode;
import application.SavableAttribute;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class TestResultNode extends DrawableNode {
    private ObservableList<TestResult> resultList = FXCollections.observableArrayList();

    public TestResultNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public TestResultNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
    }

    public TestResultNode(Double x, Double y, String containedText, Integer id, Integer programId) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, programId, id);
    }

    public void addResult(TestResult testResult) {
        class OneShotTask implements Runnable {
            private TestResult testResult;

            OneShotTask(TestResult testResult) {
                this.testResult = testResult;
            }

            public void run() {
                resultList.add(testResult);
            }
        }

        Platform.runLater(new OneShotTask(testResult));
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<SavableAttribute>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public String getNodeType() {
        return "TestResultNode";
    }

    public ObservableList<TestResult> getResultList() {
        return resultList;
    }
}
