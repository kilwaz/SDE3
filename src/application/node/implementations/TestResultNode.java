package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.test.TestStep;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestResultNode extends DrawableNode {
    private ObservableList<TestStep> resultStepList = FXCollections.observableArrayList();

    // This will make a copy of the node passed to it
    public TestResultNode(TestResultNode testResultNode) {
        this.setX(testResultNode.getX());
        this.setY(testResultNode.getY());
        this.setWidth(testResultNode.getWidth());
        this.setHeight(testResultNode.getHeight());
        this.setColor(testResultNode.getColor());
        this.setScale(testResultNode.getScale());
        this.setContainedText(testResultNode.getContainedText());
//        this.setProgramUuid(testResultNode.getProgramUuid());
        this.setNextNodeToRun(testResultNode.getNextNodeToRun());
    }

    public TestResultNode(){
        super();
    }

    public void addResult(TestStep testStep) {
        class OneShotTask implements Runnable {
            private TestStep testResult;

            OneShotTask(TestStep testResult) {
                this.testResult = testResult;
            }

            public void run() {
                resultStepList.add(testResult);
            }
        }

        Platform.runLater(new OneShotTask(testStep));
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        TableView<TestStep> resultsTable = new TableView<>();
        resultsTable.setId("resultsTable-" + getUuidStringWithoutHyphen());

        TableColumn testId = new TableColumn("ID");
        testId.setMinWidth(30);
        testId.setMaxWidth(30);
        testId.setCellValueFactory(new PropertyValueFactory<TestStep, Integer>("TestResultId"));

        TableColumn successful = new TableColumn("Pass");
        successful.setMinWidth(30);
        successful.setMaxWidth(30);
        successful.setCellValueFactory(new PropertyValueFactory<TestStep, Label>("successLabel"));

        TableColumn testString = new TableColumn("Test String");
        testString.setMinWidth(120);
        testString.setCellValueFactory(new PropertyValueFactory<TestStep, String>("testString"));

        TableColumn testType = new TableColumn("Test Type");
        testType.setMinWidth(75);
        testType.setMaxWidth(100);
        testType.setCellValueFactory(new PropertyValueFactory<TestStep, String>("testTypeName"));

        TableColumn expectedEqual = new TableColumn("Expected");
        expectedEqual.setMinWidth(100);
        expectedEqual.setCellValueFactory(new PropertyValueFactory<TestStep, String>("expectedEqual"));

        TableColumn observedEqual = new TableColumn("Observed");
        observedEqual.setMinWidth(100);
        observedEqual.setCellValueFactory(new PropertyValueFactory<TestStep, String>("observedEqual"));

        resultsTable.setItems(getResultList());
        resultsTable.getColumns().addAll(testId);
        resultsTable.getColumns().addAll(successful);
        resultsTable.getColumns().addAll(testString);
        resultsTable.getColumns().addAll(testType);
        resultsTable.getColumns().addAll(expectedEqual);
        resultsTable.getColumns().addAll(observedEqual);
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

    public ObservableList<TestStep> getResultList() {
        return resultStepList;
    }
}
