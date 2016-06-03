package application.gui.window;

import application.error.Error;
import application.gui.UI;
import application.gui.columns.testsetbatchwindow.commandparamters.ParameterNameColumn;
import application.gui.columns.testsetbatchwindow.commandparamters.ParameterValueColumn;
import application.gui.columns.testsetbatchwindow.commandview.CommandPositionColumn;
import application.gui.columns.testsetbatchwindow.commandview.HasScreenshotColumn;
import application.gui.columns.testsetbatchwindow.commandview.MainCommandColumn;
import application.gui.columns.testsetbatchwindow.commandview.RawCommandColumn;
import application.gui.columns.testsetbatchwindow.treeview.TestCaseNameColumn;
import application.gui.columns.testsetbatchwindow.treeview.TestCaseTreeObject;
import application.test.TestCommand;
import application.test.TestCommandItem;
import application.test.TestParameter;
import application.test.core.TestCase;
import application.test.core.TestSet;
import application.test.core.TestSetBatch;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
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
    private TreeTableView<TestCaseTreeObject> testCaseTreeTableView;

    private ImageView testCommandImage;
    private PropertySheet testCommandPropertySheet;

    private AnchorPane propertySheetAnchor = new AnchorPane();

    public TestSetBatchWindow(TestSetBatch testSetBatch) {
        this.testSetBatch = testSetBatch;
        init();
    }

    private void init() {
        try {
            StackPane root = new StackPane();

            this.setScene(new Scene(root, 900, 800));
            this.setTitle("Test " + testSetBatch.getCreatedTime() + " (" + testSetBatch.getCaseCount() + " cases)");

            AnchorPane anchorPane = new AnchorPane();

            createTestTree();
            createTestCommandTableView();
            createTestCommandInformation();

            HBox topSectionHBox = new HBox(5);
            VBox testCommandDetailsVBox = new VBox(5);

            testCommandImage = new ImageView();
            //testCommandImage.setFitHeight(300);
            testCommandImage.setFitWidth(300);
            testCommandImage.setPreserveRatio(true);
            testCommandParameters = new TableView<>();
            testCommandParameters.getColumns().add(new ParameterNameColumn());
            testCommandParameters.getColumns().add(new ParameterValueColumn());
            UI.setAnchorMargins(testCommandParameters, 0.0, 0.0, 0.0, 0.0);
            testCommandParameters.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            topSectionHBox.getChildren().add(testCaseTreeTableView);
            topSectionHBox.getChildren().add(testCommandTableView);
            testCommandDetailsVBox.getChildren().add(testCommandImage);
            testCommandDetailsVBox.getChildren().add(propertySheetAnchor);
            testCommandDetailsVBox.getChildren().add(testCommandParameters);
            topSectionHBox.getChildren().add(testCommandDetailsVBox);

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

    private void createTestCommandInformation() {
        createNewPropertySheet(BeanPropertyUtils.getProperties("hello"));
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
                });
    }

    private void createNewPropertySheet(ObservableList<PropertySheet.Item> items) {
        testCommandPropertySheet = new PropertySheet(items);
        UI.setAnchorMargins(testCommandPropertySheet, 0.0, 0.0, 0.0, 0.0);
        testCommandPropertySheet.setMinHeight(200);
        testCommandPropertySheet.setMinWidth(200);
        propertySheetAnchor.getChildren().clear();
        propertySheetAnchor.getChildren().add(testCommandPropertySheet);
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
                    } else {
                        testCommandTableView.setItems(null);
                    }
                });
        testCaseTreeTableView.getColumns().add(new TestCaseNameColumn());
    }
}
