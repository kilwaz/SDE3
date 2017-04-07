package sde.application.gui.window;

import sde.application.error.Error;
import sde.application.gui.UI;
import sde.application.gui.columns.testsetbatchwindow.commandparamters.ParameterNameColumn;
import sde.application.gui.columns.testsetbatchwindow.commandparamters.ParameterValueColumn;
import sde.application.gui.columns.testsetbatchwindow.commandview.*;
import sde.application.gui.columns.testsetbatchwindow.statecompare.*;
import sde.application.gui.columns.testsetbatchwindow.treeview.TestCaseNameColumn;
import sde.application.gui.columns.testsetbatchwindow.treeview.TestCaseTreeObject;
import sde.application.gui.columns.testsetbatchwindow.treeview.TestOverviewColumn;
import sde.application.net.proxy.MetaRecordedRequest;
import sde.application.test.PageStateCompare;
import sde.application.test.TestCommand;
import sde.application.test.TestCommandItem;
import sde.application.test.TestParameter;
import sde.application.test.core.TestCase;
import sde.application.test.core.TestSet;
import sde.application.test.core.TestSetBatch;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;
import sde.application.gui.columns.requesttracker.*;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

public class TestSetBatchWindow extends SDEWindow {
    private static Logger log = Logger.getLogger(TestSetBatchWindow.class);
    private TestSetBatch testSetBatch;
    private TableView<TestCommand> testCommandTableView;
    private TableView<TestParameter> testCommandParameters;
    private TableView<MetaRecordedRequest> requestTableView;
    private TableView<PageStateCompare> stateCompareTableView;
    private TableView<CompareStateElementObject> stateCompareElementTableView;
    private TreeTableView<TestCaseTreeObject> testCaseTreeTableView;
    private PopOver exceptionInformation = new PopOver();
    private TabPane testTabPane = new TabPane();
    private TextArea testLogTextArea = new TextArea();

    private ImageView testCommandImage;

    private VBox testCommandDetailsVBox;

    private AnchorPane propertySheetAnchor = new AnchorPane();
    private AnchorPane exceptionInformationAnchor = new AnchorPane();

    public TestSetBatchWindow(TestSetBatch testSetBatch) {
        super();
        this.testSetBatch = testSetBatch;
        init();
    }

    private void init() {
        try {
            StackPane root = new StackPane();

            createScene(root, 900, 800);
            this.setTitle("Test " + testSetBatch.getFormattedTime() + " (" + testSetBatch.getCaseCount() + " cases)");

            AnchorPane anchorPane = new AnchorPane();

            SplitPane testCaseSplitPane = new SplitPane();
            testCaseSplitPane.setOrientation(Orientation.HORIZONTAL);
            testCommandDetailsVBox = new VBox(5);

            testCommandImage = new ImageView();
            testCommandImage.setFitWidth(300);
            testCommandImage.setPreserveRatio(true);
            testCommandParameters = new TableView<>();
            testCommandParameters.getColumns().add(new ParameterNameColumn());
            testCommandParameters.getColumns().add(new ParameterValueColumn());
            UI.setAnchorMargins(testCommandParameters, 0.0, 0.0, 0.0, 0.0);
            UI.setAnchorMargins(testLogTextArea, 0.0, 0.0, 0.0, 0.0);
            testCommandParameters.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


            testCommandDetailsVBox.getChildren().add(testCommandImage);
            testCommandDetailsVBox.getChildren().add(propertySheetAnchor);
            testCommandDetailsVBox.getChildren().add(exceptionInformationAnchor);
            testCommandDetailsVBox.getChildren().add(testCommandParameters);
            initElements();

            testCaseSplitPane.getItems().addAll(testCaseTreeTableView, testTabPane);
            testCaseSplitPane.setDividerPositions(0.2f);

            anchorPane.getChildren().add(testCaseSplitPane);
            root.getChildren().add(anchorPane);

            UI.setAnchorMargins(root, 0.0, 0.0, 0.0, 0.0);
            UI.setAnchorMargins(anchorPane, 0.0, 0.0, 0.0, 0.0);
            UI.setAnchorMargins(testCaseSplitPane, 0.0, 0.0, 0.0, 0.0);
            UI.setAnchorMargins(testCommandDetailsVBox, 0.0, 0.0, 0.0, 0.0);

            URL urlIcon = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(urlIcon.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            Error.CREATE_THREAD_WINDOW.record().create(ex);
        }
    }

    private void initElements() {
        createTestTree();
        createTestCommandTableView();
        createTestRequestTableView();
        createStateCompareElementTableView();
        createStateCompareTableView();
        createTestCommandInformation();
        createTestTabs();
    }

    private void createTestCommandInformation() {
        createNewPropertySheet(BeanPropertyUtils.getProperties("hello"));
    }

    private void createTestTabs() {
        // Create tabs
        Tab testCommandTab = new Tab("Commands");
        Tab testRequestsTab = new Tab("Requests");
        Tab stateComparisonsTab = new Tab("State Comparison");
        Tab testLogsTab = new Tab("Logs");

        testCommandTab.setClosable(false);
        testRequestsTab.setClosable(false);
        stateComparisonsTab.setClosable(false);
        testLogsTab.setClosable(false);

        // Create anchors
        AnchorPane testCommandContent = new AnchorPane();
        AnchorPane testRequestContent = new AnchorPane();
        AnchorPane testLogContent = new AnchorPane();
        AnchorPane stateComparisonsContent = new AnchorPane();

        UI.setAnchorMargins(testCommandContent, 0.0, 0.0, 0.0, 0.0);
        UI.setAnchorMargins(testRequestContent, 0.0, 0.0, 0.0, 0.0);
        UI.setAnchorMargins(testLogContent, 0.0, 0.0, 0.0, 0.0);
        UI.setAnchorMargins(stateComparisonsContent, 0.0, 0.0, 0.0, 0.0);
        testCommandContent.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        testRequestContent.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        testLogContent.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        stateComparisonsContent.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

        HBox hBox = new HBox(5);
        hBox.getChildren().addAll(testCommandTableView, testCommandDetailsVBox);

        SplitPane stateCompareTablesSplitPane = new SplitPane();
        stateCompareTablesSplitPane.getItems().addAll(stateCompareTableView, stateCompareElementTableView);
        stateCompareTablesSplitPane.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        stateCompareTablesSplitPane.setOrientation(Orientation.VERTICAL);
        UI.setAnchorMargins(stateCompareTablesSplitPane, 0.0, 0.0, 0.0, 0.0);
        stateCompareTablesSplitPane.setDividerPositions(0.3f);

        // Set main elements to anchors of tabs
        testCommandContent.getChildren().add(hBox);
        testRequestContent.getChildren().add(requestTableView);
        testLogContent.getChildren().add(testLogTextArea);
        stateComparisonsContent.getChildren().add(stateCompareTablesSplitPane);

        // Set contents of tabs
        testCommandTab.setContent(testCommandContent);
        testRequestsTab.setContent(testRequestContent);
        testLogsTab.setContent(testLogContent);
        stateComparisonsTab.setContent(stateComparisonsContent);

        testTabPane.getTabs().addAll(testCommandTab, testRequestsTab, testLogsTab, stateComparisonsTab);
    }

    private void createStateCompareElementTableView() {
        stateCompareElementTableView = new TableView<>();
        stateCompareElementTableView.getColumns().addAll(new StateCompareElementChangeTypeColumn());
        stateCompareElementTableView.getColumns().addAll(new StateCompareElementAttributeNameColumn());
        stateCompareElementTableView.getColumns().addAll(new StateCompareElementInitialRefColumn());
        stateCompareElementTableView.getColumns().addAll(new StateCompareElementInitialValueColumn());
        stateCompareElementTableView.getColumns().addAll(new StateCompareElementFinalValueColumn());
        stateCompareElementTableView.getColumns().addAll(new StateCompareElementBeforeColumn());
        stateCompareElementTableView.getColumns().addAll(new StateCompareElementAfterColumn());
        stateCompareElementTableView.getColumns().addAll(new StateCompareElementIncreasedByColumn());
    }

    private void createStateCompareTableView() {
        stateCompareTableView = new TableView<>();
        stateCompareTableView.getColumns().addAll(new CompareStateReferenceColumn());
        stateCompareTableView.getColumns().addAll(new CompareStateBeforeReferenceColumn());
        stateCompareTableView.getColumns().addAll(new CompareStateAfterReferenceColumn());

        stateCompareTableView.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldValue, newValue) -> {
                    stateCompareElementTableView.setItems(newValue.getElementsList());
                });
    }

    private void createTestRequestTableView() {
        // Create
        requestTableView = new TableView<>();
        requestTableView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        UI.setAnchorMargins(requestTableView, 0.0, 0.0, 0.0, 0.0);
        requestTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        requestTableView.getColumns().addAll(new RequestNumberColumn());
        requestTableView.getColumns().addAll(new HostColumn());
        requestTableView.getColumns().addAll(new MethodColumn());
        requestTableView.getColumns().addAll(new URLColumn());
        requestTableView.getColumns().addAll(new HasParametersColumn());
        requestTableView.getColumns().addAll(new StatusColumn());
        requestTableView.getColumns().addAll(new MediaTypeColumn());
        requestTableView.getColumns().addAll(new ExtensionColumn());
        requestTableView.getColumns().addAll(new TitleColumn());
        requestTableView.getColumns().addAll(new SSLColumn());
        requestTableView.getColumns().addAll(new IPColumn());
        requestTableView.getColumns().addAll(new CookieColumn());
        requestTableView.getColumns().addAll(new RequestTimeColumn());
        requestTableView.getColumns().addAll(new DurationColumn());
        requestTableView.getColumns().addAll(new RequestLengthColumn());
        requestTableView.getColumns().addAll(new ResponseLengthColumn());
        requestTableView.getColumns().addAll(new RedirectColumn());
        requestTableView.getColumns().addAll(new ProxyColumn());

        // Right click context menu
        requestTableView.setRowFactory(tableView -> {
            TableRow<MetaRecordedRequest> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem inspectMenuItem = new MenuItem("Inspect");

            inspectMenuItem.setOnAction(event -> new InspectWindow(row.getItem()));
            contextMenu.getItems().add(inspectMenuItem);

            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });
    }

    private void createTestCommandTableView() {
        testCommandTableView = new TableView<>();
        UI.setAnchorMargins(testCommandTableView, 0.0, 0.0, 0.0, 0.0);
        testCommandTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        testCommandTableView.getColumns().add(new CommandOrderColumn());
        testCommandTableView.getColumns().add(new CommandLineNumberColumn());
        testCommandTableView.getColumns().add(new MainCommandColumn());
        testCommandTableView.getColumns().add(new HasScreenshotColumn());
        testCommandTableView.getColumns().add(new RawCommandColumn());
        testCommandTableView.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldTestCommand, newTestCommand) -> {
                    BufferedImage bufferedImage = newTestCommand.getScreenshot().getScreenshotFromDatabase();
                    WritableImage writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
                    SwingFXUtils.toFXImage(bufferedImage, writableImage);
                    testCommandImage.setImage(writableImage);
                    createNewPropertySheet(BeanPropertyUtils.getProperties(new TestCommandItem(newTestCommand)));
                    testCommandParameters.setItems(newTestCommand.getTestParameters());
                    createExceptionLabel(newTestCommand);
                });
    }

    private void createNewPropertySheet(ObservableList<PropertySheet.Item> items) {
        PropertySheet testCommandPropertySheet = new PropertySheet(items);
        UI.setAnchorMargins(testCommandPropertySheet, 0.0, 0.0, 0.0, 0.0);
        testCommandPropertySheet.setMinHeight(200);
        testCommandPropertySheet.setMinWidth(200);
        propertySheetAnchor.getChildren().clear();
        propertySheetAnchor.getChildren().add(testCommandPropertySheet);
    }

    private Label createExceptionLabel(TestCommand testCommand) {
        Label exceptionLabel = new Label();

        if (testCommand.hasException()) {
            exceptionLabel.setText("Exception Info");

            exceptionLabel.setOnMouseClicked(event -> {
                VBox vBox = new VBox(5);
                vBox.setPadding(new Insets(10, 6, 10, 6));
                vBox.setAlignment(Pos.CENTER);
                try {
                    Exception testException = testCommand.getTestCommandError().getException();
                    vBox.getChildren().add(new Label("Message: " + testException.getLocalizedMessage()));
                    vBox.getChildren().add(new Label("Cause: " + testException.getCause()));

                    exceptionInformation.show(exceptionLabel, event.getScreenX(), event.getScreenY());
                    exceptionInformation.setContentNode(vBox);
                } catch (Exception ex) {
                    log.error(ex);
                }
            });
            exceptionLabel.setOnMouseEntered(event -> {
                exceptionLabel.setUnderline(true);
                this.getScene().setCursor(Cursor.HAND);
            });

            exceptionLabel.setOnMouseExited(event -> {
                exceptionLabel.setUnderline(false);
                this.getScene().setCursor(Cursor.DEFAULT);
            });
        } else {
            exceptionLabel.setText("No Exception");
        }

        exceptionLabel.setTextFill(Color.GRAY);
        exceptionInformationAnchor.getChildren().clear();
        exceptionInformationAnchor.getChildren().add(exceptionLabel);
        return exceptionLabel;
    }

    private void createTestTree() {
        TreeItem<TestCaseTreeObject> testRoot = TestCaseTreeObject.createTreeItem();
        testRoot.setExpanded(true);
        for (TestSet testSet : testSetBatch.getTestSets()) {
            TreeItem<TestCaseTreeObject> testCaseRoot = TestCaseTreeObject.createTreeItem(testSet);
            for (TestCase testCase : (List<TestCase>) testSet.getTestCases()) {
                testCaseRoot.getChildren().add(TestCaseTreeObject.createTreeItem(testCase));
            }
            testRoot.getChildren().add(testCaseRoot);
        }

        testCaseTreeTableView = new TreeTableView<>(testRoot);
        testCaseTreeTableView.setPrefWidth(120);
        testCaseTreeTableView.setMaxWidth(300);
        testCaseTreeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        testCaseTreeTableView.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldValue, newValue) -> {
                    TestCaseTreeObject testCaseTreeObject = newValue.getValue();
                    if (testCaseTreeObject.getType().equals(TestCaseTreeObject.TEST_CASE)) {
                        testCommandTableView.setItems(testCaseTreeObject.getTestCase().getTestCommands());
                        requestTableView.setItems(testCaseTreeObject.getTestCase().getTestRequests());
                        ObservableList<PageStateCompare> list = (ObservableList<PageStateCompare>) testCaseTreeObject.getTestCase().getPageCapturesCompares();
                        stateCompareTableView.setItems(list);
                        if (list.size() > 0) {
                            stateCompareElementTableView.setItems(list.get(0).getElementsList());
                        } else {
                            stateCompareElementTableView.setItems(null);
                        }

                        testLogTextArea.clear();
                        testLogTextArea.setText(testCaseTreeObject.getTestCase().getLogMessages());
                    } else {
                        testCommandTableView.setItems(null);
                        requestTableView.setItems(null);
                        stateCompareTableView.setItems(null);
                        stateCompareElementTableView.setItems(null);
                        testLogTextArea.clear();
                    }
                });
        testCaseTreeTableView.getColumns().add(new TestCaseNameColumn());
        testCaseTreeTableView.getColumns().add(new TestOverviewColumn());
    }
}
