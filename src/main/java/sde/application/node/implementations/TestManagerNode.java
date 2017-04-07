package sde.application.node.implementations;

import sde.application.data.SavableAttribute;
import sde.application.data.model.dao.LinkedTestCaseDAO;
import sde.application.gui.Controller;
import sde.application.gui.Program;
import sde.application.gui.UI;
import sde.application.gui.columns.testnode.EnabledColumn;
import sde.application.gui.columns.testnode.HierarchyTreeRow;
import sde.application.gui.columns.testnode.LinkedTestCaseTreeObject;
import sde.application.gui.columns.testnode.NodeNameColumn;
import sde.application.gui.columns.testsetbatch.CasesColumn;
import sde.application.gui.columns.testsetbatch.CreatedColumn;
import sde.application.gui.window.TestSetBatchWindow;
import sde.application.node.design.DrawableNode;
import sde.application.node.objects.LinkedTestCase;
import sde.application.test.core.TestSetBatch;
import sde.application.data.export.CreateTestResultExcel;
import sde.application.utils.NodeRunParams;
import sde.application.utils.SDEThread;
import sde.application.utils.SDEThreadCollection;
import sde.application.utils.managers.ThreadManager;
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
    private static Logger log = Logger.getLogger(TestManagerNode.class);

    private TabPane testManagerTabPane = null;
    private Tab linkedTestsTab;
    private Tab resultsTab;
    private Button addCaseTestNodeButton = null;
    private TableView<TestSetBatch> testSetBatchTableView;
    private TreeTableView<LinkedTestCaseTreeObject> linkedTestsTreeTableView;
    private TreeItem<LinkedTestCaseTreeObject> testRoot;

    private ObservableList<LinkedTestCase> linkedTestCases = null;
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
            Boolean alreadyExists = false;
            for (LinkedTestCase linkedTestCase : getLinkedTestCases()) {
                // If it already exists, don't add it again
                if (linkedTestCase.getTestCaseNode() != null && linkedTestCase.getTestCaseNode().getUuidString().equals(testCaseNode.getUuidString())) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                LinkedTestCase linkedTestCase = LinkedTestCase.create(LinkedTestCase.class);
                linkedTestCase.setTestCaseNode((TestCaseNode) testCaseNode);
                linkedTestCase.setEnabled(false);
                linkedTestCase.setTestManagerNode(this);
                linkedTestCase.save();

                testRoot.getChildren().add(LinkedTestCaseTreeObject.createTreeItem(linkedTestCase));

                getLinkedTestCases().add(linkedTestCase);
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
        createTestCaseTree();

        AnchorPane linkedTestAnchorPane = new AnchorPane();
        linkedTestAnchorPane.getChildren().add(linkedTestsTreeTableView);

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
            MenuItem exportMenuItem = new MenuItem("ExportSheet...");

            inspectMenuItem.setOnAction(event -> new TestSetBatchWindow(row.getItem()));
            exportMenuItem.setOnAction(event -> CreateTestResultExcel.outputExcelTestResults(row.getItem()));

            contextMenu.getItems().add(inspectMenuItem);
            contextMenu.getItems().add(exportMenuItem);

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
        UI.setAnchorMargins(testSetBatchTableView, 0.0, 0.0, 0.0, 0.0);
        UI.setAnchorMargins(resultsAnchorPane, 10.0, 0.0, 11.0, 0.0);
        UI.setAnchorMargins(linkedTestsTreeTableView, 0.0, 0.0, 0.0, 0.0);
        UI.setAnchorMargins(linkedTestAnchorPane, 10.0, 0.0, 11.0, 0.0);

        testSetBatchTableView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        linkedTestsTreeTableView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

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

    public ObservableList<LinkedTestCase> getLinkedTestCases() {
        if (linkedTestCases == null) {
            linkedTestCases = FXCollections.observableArrayList();
            LinkedTestCaseDAO linkedTestCaseDAO = new LinkedTestCaseDAO();
            List<LinkedTestCase> loadedLinkedTestCases = linkedTestCaseDAO.getLinkedTestCases(this);
            linkedTestCases.addAll(loadedLinkedTestCases);
        }
        return linkedTestCases;
    }

    public ObservableList<TestSetBatch> getTestSetBatchResults() {
        return testSetBatchResults;
    }

    private TreeItem<LinkedTestCaseTreeObject> buildTestCaseTree(LinkedTestCaseTreeObject linkedTestCaseTreeObject) {
        for (LinkedTestCase childTestCase : linkedTestCaseTreeObject.getLinkedTestCase().getChildTestCases()) {
            linkedTestCaseTreeObject.getTreeItem().getChildren().add(buildTestCaseTree(LinkedTestCaseTreeObject.createTreeItem(childTestCase).getValue()));
        }

        return linkedTestCaseTreeObject.getTreeItem();
    }

    private void createTestCaseTree() {
        testRoot = LinkedTestCaseTreeObject.createTreeItem();
        testRoot.setExpanded(true);
        for (LinkedTestCase linkedTestCase : getLinkedTestCases()) {
            if (linkedTestCase.getParentTestCase() == null) { // Top level test case
                testRoot.getChildren().add(buildTestCaseTree(LinkedTestCaseTreeObject.createTreeItem(linkedTestCase).getValue()));
            }
        }

        linkedTestsTreeTableView = new TreeTableView<>(testRoot);
        linkedTestsTreeTableView.setId("linkedTestsTable-" + getUuidStringWithoutHyphen());

        // Right click context menu
        linkedTestsTreeTableView.setRowFactory(tableView -> {
            TreeTableRow<LinkedTestCaseTreeObject> row = new HierarchyTreeRow();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem removeMenuItem = new MenuItem("Remove");
            MenuItem enableMenuItem = new MenuItem("Enable");
            MenuItem disableMenuItem = new MenuItem("Disable");
            MenuItem testGroupMenuItem = new MenuItem("New Test Group");

            enableMenuItem.setOnAction(event -> { // Enable action
                LinkedTestCaseTreeObject linkedTestCaseTreeObject = row.getItem();
                linkedTestCaseTreeObject.getLinkedTestCase().setEnabled(true);
                linkedTestCaseTreeObject.getLinkedTestCase().save();
            });

            disableMenuItem.setOnAction(event -> { // Disable action
                LinkedTestCaseTreeObject linkedTestCaseTreeObject = row.getItem();
                linkedTestCaseTreeObject.getLinkedTestCase().setEnabled(false);
                linkedTestCaseTreeObject.getLinkedTestCase().save();
            });

            removeMenuItem.setOnAction(event -> { // Remove action
                LinkedTestCaseTreeObject linkedTestCaseTreeObject = row.getItem();
                linkedTestCaseTreeObject.getLinkedTestCase().delete();
                testRoot.getChildren().remove(linkedTestCaseTreeObject.getTreeItem());
            });

            testGroupMenuItem.setOnAction(event -> { // New test group action
                LinkedTestCaseTreeObject clickedRowTreeObject = row.getItem();
                LinkedTestCase testCaseGroup = LinkedTestCase.create(LinkedTestCase.class);
                testCaseGroup.setType("Group");
                testCaseGroup.setParentTestCase(clickedRowTreeObject.getLinkedTestCase());
                testCaseGroup.setTestManagerNode(this);
                testCaseGroup.save();

                clickedRowTreeObject.getTreeItem().getChildren().add(LinkedTestCaseTreeObject.createTreeItem(testCaseGroup));
            });

            contextMenu.getItems().add(enableMenuItem);
            contextMenu.getItems().add(disableMenuItem);
            contextMenu.getItems().add(removeMenuItem);
            contextMenu.getItems().add(testGroupMenuItem);

            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });

        linkedTestsTab = new Tab("Available Tests");
        linkedTestsTab.setClosable(false);

        linkedTestsTreeTableView.getColumns().addAll(new NodeNameColumn());
        linkedTestsTreeTableView.getColumns().addAll(new EnabledColumn());
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        TestSetBatch testSetBatch = new TestSetBatch();
        String uniqueReference = this.toString() + "-" + UUID.randomUUID().toString();

        addTestSetBatchResult(testSetBatch);
        testSetBatch.setParentNode(this);
        testSetBatch.save();

        NodeRunParams nodeRunParams1 = new NodeRunParams();
        nodeRunParams1.setOneTimeVariable(testSetBatch);

        for (LinkedTestCase linkedTestCase : getLinkedTestCases()) {
            if (linkedTestCase.isEnabled() && linkedTestCase.getTestCaseNode() != null) {
                TestCaseNode testCaseNode = linkedTestCase.getTestCaseNode();
                SDEThread sdeThread = Program.runHelper(testCaseNode.getContainedText(), testCaseNode.getProgram().getFlowController().getReferenceID(), null, false, true, uniqueReference, nodeRunParams1);
            }
        }

        // Waits for all previous threads to finish
        SDEThreadCollection sdeThreadCollection = ThreadManager.getInstance().getThreadCollection(uniqueReference);
        if (sdeThreadCollection != null) {
            sdeThreadCollection.join();
        }
    }
}
