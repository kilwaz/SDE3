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

        Tab tab = new Tab();

        AnchorPane tabAnchorPane = new AnchorPane();
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

        tabAnchorPane.setMaxHeight(Integer.MAX_VALUE);
        tabAnchorPane.setMaxWidth(Integer.MAX_VALUE);
        AnchorPane.setBottomAnchor(tabAnchorPane, 0.0);
        AnchorPane.setLeftAnchor(tabAnchorPane, 0.0);
        AnchorPane.setRightAnchor(tabAnchorPane, 0.0);
        AnchorPane.setTopAnchor(tabAnchorPane, 0.0);

        tabAnchorPane.getChildren().add(controller.createNodeNameField(this));
        tabAnchorPane.getChildren().add(controller.createNodeNameLabel());
        tabAnchorPane.getChildren().add(resultsTable);
        tab.setText(getContainedText());
        tab.setId(getId().toString());
        tab.setContent(tabAnchorPane);

        return tab;
    }

    public Color getFillColour() {
        return Color.LIGHTSTEELBLUE;
    }

    public String getNodeType() {
        return "TestResultNode";
    }

    public ObservableList<TestResult> getResultList() {
        return resultList;
    }
}
