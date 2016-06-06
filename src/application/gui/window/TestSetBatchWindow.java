package application.gui.window;

import application.error.Error;
import application.gui.UI;
import application.gui.columns.requesttracker.*;
import application.gui.columns.testsetbatchwindow.commandparamters.ParameterNameColumn;
import application.gui.columns.testsetbatchwindow.commandparamters.ParameterValueColumn;
import application.gui.columns.testsetbatchwindow.commandview.CommandPositionColumn;
import application.gui.columns.testsetbatchwindow.commandview.HasScreenshotColumn;
import application.gui.columns.testsetbatchwindow.commandview.MainCommandColumn;
import application.gui.columns.testsetbatchwindow.commandview.RawCommandColumn;
import application.gui.columns.testsetbatchwindow.treeview.TestCaseNameColumn;
import application.gui.columns.testsetbatchwindow.treeview.TestCaseTreeObject;
import application.net.proxy.RecordedRequest;
import application.test.TestCommand;
import application.test.TestCommandItem;
import application.test.TestParameter;
import application.test.core.TestCase;
import application.test.core.TestSet;
import application.test.core.TestSetBatch;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

public class TestSetBatchWindow extends Stage {
    private static Logger log = Logger.getLogger(TestSetBatchWindow.class);
    private TestSetBatch testSetBatch;
    private TableView<TestCommand> testCommandTableView;
    private TableView<TestParameter> testCommandParameters;
    private TableView<RecordedRequest> requestTableView;
    private TreeTableView<TestCaseTreeObject> testCaseTreeTableView;
    private PopOver exceptionInformation = new PopOver();
    private TabPane testTabPane = new TabPane();

    private ImageView testCommandImage;

    private VBox testCommandDetailsVBox;

    private AnchorPane propertySheetAnchor = new AnchorPane();
    private AnchorPane exceptionInformationAnchor = new AnchorPane();

    public TestSetBatchWindow(TestSetBatch testSetBatch) {
        this.testSetBatch = testSetBatch;
        init();
    }

    private void init() {
        try {
            StackPane root = new StackPane();

            this.setScene(new Scene(root, 900, 800));
            this.setTitle("Test " + testSetBatch.getFormattedTime() + " (" + testSetBatch.getCaseCount() + " cases)");

            AnchorPane anchorPane = new AnchorPane();

            HBox topSectionHBox = new HBox(5);
            testCommandDetailsVBox = new VBox(5);

            testCommandImage = new ImageView();
            testCommandImage.setFitWidth(300);
            testCommandImage.setPreserveRatio(true);
            testCommandParameters = new TableView<>();
            testCommandParameters.getColumns().add(new ParameterNameColumn());
            testCommandParameters.getColumns().add(new ParameterValueColumn());
            UI.setAnchorMargins(testCommandParameters, 0.0, 0.0, 0.0, 0.0);
            testCommandParameters.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


            testCommandDetailsVBox.getChildren().add(testCommandImage);
            testCommandDetailsVBox.getChildren().add(propertySheetAnchor);
            testCommandDetailsVBox.getChildren().add(exceptionInformationAnchor);
            testCommandDetailsVBox.getChildren().add(testCommandParameters);
            initElements();

            topSectionHBox.getChildren().add(testCaseTreeTableView);
            topSectionHBox.getChildren().add(testTabPane);

            anchorPane.getChildren().add(topSectionHBox);
            root.getChildren().add(anchorPane);

            UI.setAnchorMargins(root, 0.0, 0.0, 0.0, 0.0);
            UI.setAnchorMargins(anchorPane, 0.0, 0.0, 0.0, 0.0);
            UI.setAnchorMargins(topSectionHBox, 0.0, 0.0, 0.0, 0.0);
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
        createTestCommandInformation();
        createTestTabs();
    }

    private void createTestCommandInformation() {
        createNewPropertySheet(BeanPropertyUtils.getProperties("hello"));
    }

    private void createTestTabs() {
        // Create tabs
        Tab testCommandTab = new Tab("Test Commands");
        Tab testRequestsTab = new Tab("Test Requests");

        AnchorPane testCommandContent = new AnchorPane();
        AnchorPane testRequestContent = new AnchorPane();

        UI.setAnchorMargins(testCommandContent, 0.0, 0.0, 0.0, 0.0);
        UI.setAnchorMargins(testRequestContent, 0.0, 0.0, 0.0, 0.0);
        testCommandContent.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        testRequestContent.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

        HBox hBox = new HBox(5);
        hBox.getChildren().add(testCommandTableView);
        hBox.getChildren().add(testCommandDetailsVBox);

        testCommandContent.getChildren().add(hBox);
        testRequestContent.getChildren().add(requestTableView);

        testCommandTab.setContent(testCommandContent);
        testRequestsTab.setContent(testRequestContent);

        testTabPane.getTabs().addAll(testCommandTab, testRequestsTab);
    }

    private void createTestRequestTableView() {
        // Create
        requestTableView = new TableView<>();
        requestTableView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        UI.setAnchorMargins(requestTableView, 0.0, 0.0, 0.0, 0.0);
        requestTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        //requestTableView.setItems(getResultList());
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
            TableRow<RecordedRequest> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem inspectMenuItem = new MenuItem("Inspect");

            inspectMenuItem.setOnAction(event -> new RequestInspectWindow(row.getItem()));
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
//        testCommandTableView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        testCommandTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        testCommandTableView.getColumns().add(new CommandPositionColumn());
        testCommandTableView.getColumns().add(new MainCommandColumn());
        testCommandTableView.getColumns().add(new HasScreenshotColumn());
        testCommandTableView.getColumns().add(new RawCommandColumn());
        testCommandTableView.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldTestCommand, newTestCommand) -> {
                    BufferedImage bufferedImage = newTestCommand.getScreenshot();
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
        testCaseTreeTableView.setPrefWidth(150);
        testCaseTreeTableView.setMinWidth(120);
        testCaseTreeTableView.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldValue, newValue) -> {
                    TestCaseTreeObject testCaseTreeObject = newValue.getValue();
                    if (testCaseTreeObject.getType().equals(TestCaseTreeObject.TEST_CASE)) {
                        testCommandTableView.setItems(testCaseTreeObject.getTestCase().getTestCommands());
                        requestTableView.setItems(testCaseTreeObject.getTestCase().getTestRequests());
                    } else {
                        testCommandTableView.setItems(null);
                        requestTableView.setItems(null);
                    }
                });
        testCaseTreeTableView.getColumns().add(new TestCaseNameColumn());
    }
}
