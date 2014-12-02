package application.node;

import application.data.SavableAttribute;
import application.gui.Controller;
import application.test.TestResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class TestResultNode extends DrawableNode {
    private ObservableList<TestResult> resultList = FXCollections.observableArrayList();

    // This will make a copy of the node passed to it
    public TestResultNode(TestResultNode testResultNode) {
        this.setId(-1);
        this.setX(testResultNode.getX());
        this.setY(testResultNode.getY());
        this.setWidth(testResultNode.getWidth());
        this.setHeight(testResultNode.getHeight());
        this.setColor(testResultNode.getColor());
        this.setScale(testResultNode.getScale());
        this.setContainedText(testResultNode.getContainedText());
        this.setProgramId(testResultNode.getProgramId());
        this.setNextNodeToRun(testResultNode.getNextNodeToRun());

        // Do we want to copy all the test result data as well?
        //this.resultList = FXCollections.observableArrayList();
        //this.resultList.addAll(testResultNode.getResultList());
    }

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

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        TableView<TestResult> resultsTable = new TableView<TestResult>();
        resultsTable.setId("resultsTable-" + getId());

        TableColumn expectedOutput = new TableColumn("Expected Output");
        expectedOutput.setMinWidth(120);
        expectedOutput.setCellValueFactory(new PropertyValueFactory<TestResult, String>("outcome"));

        TableColumn actualOutput = new TableColumn("Actual Output");
        actualOutput.setMinWidth(120);
        actualOutput.setCellValueFactory(new PropertyValueFactory<TestResult, String>("expected"));

        TableColumn duration = new TableColumn("Duration");
        duration.setMinWidth(120);
        duration.setCellValueFactory(new PropertyValueFactory<TestResult, String>("duration"));

        resultsTable.setItems(getResultList());
        resultsTable.getColumns().addAll(expectedOutput);
        resultsTable.getColumns().addAll(actualOutput);
        resultsTable.getColumns().addAll(duration);
        resultsTable.setLayoutX(11);
        resultsTable.setLayoutY(50);

        resultsTable.setMaxHeight(Integer.MAX_VALUE);
        resultsTable.setMaxWidth(Integer.MAX_VALUE);
        AnchorPane.setBottomAnchor(resultsTable, 0.0);
        AnchorPane.setLeftAnchor(resultsTable, 11.0);
        AnchorPane.setRightAnchor(resultsTable, 0.0);
        AnchorPane.setTopAnchor(resultsTable, 50.0);
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        anchorPane.getChildren().add(resultsTable);

        return tab;
    }

    public ObservableList<TestResult> getResultList() {
        return resultList;
    }
}
