package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.Program;
import application.gui.UI;
import application.gui.window.TestSetBatchWindow;
import application.node.design.DrawableNode;
import application.gui.columns.testnode.EnabledColumn;
import application.gui.columns.testnode.NodeNameColumn;
import application.gui.columns.testsetbatch.CasesColumn;
import application.gui.columns.testsetbatch.CreatedColumn;
import application.test.core.TestSetBatch;
import application.utils.NodeRunParams;
import application.utils.SDEThread;
import application.utils.SDEThreadCollection;
import application.utils.managers.ThreadManager;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestManagerNode extends DrawableNode {
    private static Logger log = Logger.getLogger(BashNode.class);

    private TabPane testManagerTabPane = null;
    private Tab linkedTestsTab;
    private Tab resultsTab;
    private Button addCaseTestNodeButton = null;
    private TableView<TestSetBatch> testSetBatchTableView;

    private ObservableList<TestCaseNode> linkedTests = FXCollections.observableArrayList();
    private ObservableList<TestSetBatch> testSetBatchResults = FXCollections.observableArrayList();

    // This will make a copy of the node passed to it
    public TestManagerNode(TestManagerNode testManagerNode) {
        this.setX(testManagerNode.getX());
        this.setY(testManagerNode.getY());
        this.setWidth(testManagerNode.getWidth());
        this.setHeight(testManagerNode.getHeight());
        this.setColor(testManagerNode.getColor());
        this.setScale(testManagerNode.getScale());
        this.setContainedText(testManagerNode.getContainedText());
        this.setNextNodeToRun(testManagerNode.getNextNodeToRun());
    }

    public TestManagerNode() {
        super();
        super.setColor(Color.BLACK);
    }

    public void addTestCaseNodes(List<DrawableNode> testCaseNodes) {
        if (testCaseNodes != null) {
            testCaseNodes.forEach(this::addTestCaseNode);
        }
    }

    public void addTestCaseNode(DrawableNode testCaseNode) {
        if (testCaseNode != null && testCaseNode instanceof TestCaseNode) {
            if (!linkedTests.contains(testCaseNode)) {
                linkedTests.add((TestCaseNode) testCaseNode);
            }
        }
    }

    public void startNodeSelectIcon() {
        if (addCaseTestNodeButton != null) {
            addCaseTestNodeButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.HAND_POINTER_ALT).build());
        }
    }

    public void endNodeSelectIcon() {
        if (addCaseTestNodeButton != null) {
            addCaseTestNodeButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.PLUS).build());
        }
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this, false);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        VBox vBox = new VBox(5);
        addCaseTestNodeButton = new Button();
        addCaseTestNodeButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.PLUS).build());
        addCaseTestNodeButton.setPrefWidth(35);
        addCaseTestNodeButton.setTooltip(new Tooltip("Add Test Case Node"));
        addCaseTestNodeButton.setId("addTestCaseNodeButton-" + getUuidString());
        addCaseTestNodeButton.setOnAction(event -> {
            if (controller.isSelectingTestNode()) {
                controller.endTestManagerNodeSelect();
            } else {
                controller.startTestManagerNodeSelect(this);
            }
        });
        vBox.getChildren().add(addCaseTestNodeButton);

        // Setup main tab pane
        testManagerTabPane = new TabPane();

        // Setup available tests tab
        TableView<TestCaseNode> linkedTestsTableView = new TableView<>();
        linkedTestsTableView.setId("linkedTestsTable-" + getUuidStringWithoutHyphen());

        linkedTestsTab = new Tab("Available Tests");
        linkedTestsTab.setClosable(false);

        AnchorPane linkedTestAnchorPane = new AnchorPane();
        linkedTestAnchorPane.getChildren().add(linkedTestsTableView);

        linkedTestsTableView.setItems(getLinkedTests());
        linkedTestsTableView.getColumns().addAll(new NodeNameColumn());
        linkedTestsTableView.getColumns().addAll(new EnabledColumn());

        // Setup results tab
        testSetBatchTableView = new TableView<>();
        testSetBatchTableView.setId("testSetBatchResultsTable-" + getUuidStringWithoutHyphen());

        testSetBatchTableView.setItems(getTestSetBatchResults());
        testSetBatchTableView.getColumns().addAll(new CreatedColumn());
        testSetBatchTableView.getColumns().addAll(new CasesColumn());

        // Right click context menu
        testSetBatchTableView.setRowFactory(tableView -> {
            TableRow<TestSetBatch> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem inspectMenuItem = new MenuItem("Inspect");

            inspectMenuItem.setOnAction(event -> new TestSetBatchWindow(row.getItem()));

            contextMenu.getItems().add(inspectMenuItem);

            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });

        resultsTab = new Tab("Results");
        resultsTab.setClosable(false);

        AnchorPane resultsAnchorPane = new AnchorPane();
        resultsAnchorPane.getChildren().add(testSetBatchTableView);

        UI.setAnchorMargins(vBox, 50.0, 0.0, 11.0, 0.0);
        UI.setAnchorMargins(testManagerTabPane, 50.0, 0.0, 0.0, 0.0);
        UI.setAnchorMargins(testSetBatchTableView, 25.0, 0.0, 11.0, 0.0);
        UI.setAnchorMargins(resultsAnchorPane, 10.0, 0.0, 11.0, 0.0);
        UI.setAnchorMargins(linkedTestsTableView, 25.0, 0.0, 11.0, 0.0);
        UI.setAnchorMargins(linkedTestAnchorPane, 10.0, 0.0, 11.0, 0.0);

        testSetBatchTableView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        linkedTestsTableView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

        linkedTestsTab.setContent(linkedTestAnchorPane);
        resultsTab.setContent(resultsAnchorPane);

        testManagerTabPane.getTabs().addAll(linkedTestsTab);
        testManagerTabPane.getTabs().addAll(resultsTab);
        testManagerTabPane.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        vBox.getChildren().addAll(testManagerTabPane);
        anchorPane.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        anchorPane.getChildren().add(vBox);

        tab.setContent(anchorPane);

        return tab;
    }

    public void addTestSetBatchResult(TestSetBatch testSetBatch) {
        testSetBatchResults.add(testSetBatch);
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public ObservableList<TestCaseNode> getLinkedTests() {
        return linkedTests;
    }

    public ObservableList<TestSetBatch> getTestSetBatchResults() {
        return testSetBatchResults;
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        TestSetBatch testSetBatch = new TestSetBatch();
        String uniqueReference = this.toString() + "-" + UUID.randomUUID().toString();

        addTestSetBatchResult(testSetBatch);

        NodeRunParams nodeRunParams1 = new NodeRunParams();
        nodeRunParams1.setOneTimeVariable(testSetBatch);

        for (TestCaseNode testCaseNode : linkedTests) {
            SDEThread sdeThread = Program.runHelper(testCaseNode.getContainedText(), testCaseNode.getProgram().getFlowController().getReferenceID(), null, false, true, uniqueReference, nodeRunParams1);
        }

        // Waits for all previous threads to finish
        SDEThreadCollection sdeThreadCollection = ThreadManager.getInstance().getThreadCollection(uniqueReference);
        if (sdeThreadCollection != null) {
            sdeThreadCollection.join();
        }
    }
}
