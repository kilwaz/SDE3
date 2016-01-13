package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.UI;
import application.node.design.DrawableNode;
import application.node.objects.Test;
import application.test.TestRunner;
import application.utils.NodeRunParams;
import application.utils.SDEThread;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BulkTestNode extends DrawableNode {
    private ObservableList<TestRunner> tests = FXCollections.observableArrayList();

    private static Logger log = Logger.getLogger(BulkTestNode.class);

    // This will make a copy of the node passed to it
    public BulkTestNode(TestNode testNode) {
        this.setX(testNode.getX());
        this.setY(testNode.getY());
        this.setY(testNode.getY());
        this.setWidth(testNode.getWidth());
        this.setHeight(testNode.getHeight());
        this.setColor(testNode.getColor());
        this.setScale(testNode.getScale());
        this.setContainedText(testNode.getContainedText());
        this.setNextNodeToRun(testNode.getNextNodeToRun());
    }

    public BulkTestNode() {
        super();
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();
        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        TableView<TestRunner> testRunnerTableView = new TableView<>();

        TableColumn testStatus = new TableColumn("Status");
        testStatus.setMinWidth(30);
        testStatus.setCellValueFactory(new PropertyValueFactory<TestRunner, String>("StatusText"));

        testRunnerTableView.getColumns().addAll(testStatus);
        testRunnerTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        testRunnerTableView.setItems(getTests());

        testRunnerTableView.setLayoutX(11);
        testRunnerTableView.setLayoutY(50);
        testRunnerTableView.setMaxHeight(Integer.MAX_VALUE);
        testRunnerTableView.setMaxWidth(Integer.MAX_VALUE);

        UI.setAnchorMargins(testRunnerTableView, 50.0, 0.0, 11.0, 0.0);

        anchorPane.getChildren().add(testRunnerTableView);

        return tab;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void addTest(Test test) {
        TestRunner testRunner = new TestRunner(test, getProgram());
        tests.add(testRunner);
    }

    public void startAllTests() {
        for (TestRunner testRunner : getTests()) {
            SDEThread sdeThread = new SDEThread(testRunner, "Browser test");
        }
    }

    // Handles and runs all text typed in
    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {

    }

    public ObservableList<TestRunner> getTests() {
        return tests;
    }
}
