package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.AceTextArea;
import application.gui.Controller;
import application.gui.UI;
import application.net.proxy.snoop.HttpProxyServer;
import application.net.websocket.Listener;
import application.node.design.DrawableNode;
import application.node.objects.Test;
import application.test.TestRunner;
import application.test.core.TestStructure;
import application.utils.BrowserHelper;
import application.utils.NodeRunParams;
import application.utils.SDEThread;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

public class TestNode extends DrawableNode {
    private static Logger log = Logger.getLogger(TestNode.class);
    private Test test;
    private AceTextArea aceTextArea = null;
    private Tab testRawEditTab;
    private Tab testHelperEditTab;
    private List<Tab> runningTabs = new ArrayList<>();
    private TabPane testNodeTabPane = null;
    private TestStructure testStructure = null;
    private VBox vBoxEdit;
    private ScrollPane testHelperAnchorScrollPane;
    private AnchorPane testHelperAnchorPane;

    // This will make a copy of the node passed to it
    public TestNode(TestNode testNode) {
        this.setX(testNode.getX());
        this.setY(testNode.getY());
        this.setY(testNode.getY());
        this.setWidth(testNode.getWidth());
        this.setHeight(testNode.getHeight());
        this.setColor(testNode.getColor());
        this.setScale(testNode.getScale());
        this.setContainedText(testNode.getContainedText());
        this.setNextNodeToRun(testNode.getNextNodeToRun());

        this.test = new Test(this);
        this.setTest(testNode.getTest().getText());
    }

    public TestNode() {
        super();
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();
        Tab tab = controller.createDefaultNodeTab(this, false);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        // Setup helper interface tab
        testHelperAnchorScrollPane = new ScrollPane();
        testHelperAnchorPane = new AnchorPane();

        // Setup main tab pane
        testNodeTabPane = new TabPane();
        testNodeTabPane.getSelectionModel().selectedItemProperty().addListener(
                (ov, t, t1) -> {
                    if ("Raw Script".equals(ov.getValue().getText()) && t != null) {
                        if (test != null) {
                            test.setText(testStructure.toScript());
                            // Is this a really long winded way of doing this, creating a new browser view and populating it again?
                            vBoxEdit.getChildren().remove(aceTextArea);
                            aceTextArea = new AceTextArea(this, "ace/mode/sde");
                            vBoxEdit.getChildren().add(aceTextArea);
                        }
                    } else if ("Helper".equals(ov.getValue().getText())) {
                        testStructure = TestStructure.create(test);
                        testHelperAnchorPane.getChildren().clear();
                        testHelperAnchorPane.getChildren().add(testStructure.getInterface());
                        testHelperAnchorScrollPane.setContent(testHelperAnchorPane);
                    }
                });

        // Setup raw script tab
        testRawEditTab = new Tab("Raw Script");
        testRawEditTab.setClosable(false);

        vBoxEdit = new VBox(5);
        AnchorPane testEditAnchorPane = new AnchorPane();

        Button recordButton = new Button();
        recordButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.DOT_CIRCLE_ALT).build());

        recordButton.setPrefWidth(35);
        recordButton.setTooltip(new Tooltip("Record from browser"));
        recordButton.setId("recordButton-" + getUuidString());
        recordButton.setOnAction(event -> {
            Listener listener = new Listener();
            SDEThread thread = new SDEThread(listener, "Websocket listener", null, true);
            HttpProxyServer httpProxyServer = new HttpProxyServer();
            SDEThread webProxyThread = new SDEThread(httpProxyServer, "Running proxy server - for recording browser", null, true);
            WebDriver driver = BrowserHelper.getChrome();
            driver.get("about:blank");
        });

        aceTextArea = new AceTextArea(this, "ace/mode/sde");

        HBox hBox = new HBox(5);
        hBox.getChildren().add(recordButton);
        hBox.setAlignment(Pos.BASELINE_LEFT);

        vBoxEdit.getChildren().add(hBox);
        vBoxEdit.getChildren().add(aceTextArea);

        UI.setAnchorMargins(vBoxEdit, 10.0, 0.0, 11.0, 0.0);
        UI.setAnchorMargins(testEditAnchorPane, 0.0, 0.0, 0.0, 0.0);

        testEditAnchorPane.getChildren().add(vBoxEdit);
        testRawEditTab.setContent(testEditAnchorPane);

        testHelperEditTab = new Tab("Helper");
        testHelperEditTab.setClosable(false);

        UI.setAnchorMargins(testHelperAnchorPane, 0.0, 0.0, 0.0, 0.0);
        UI.setAnchorMargins(testHelperAnchorScrollPane, 0.0, 0.0, 0.0, 0.0);
        testHelperAnchorScrollPane.setContent(testHelperAnchorPane);
        testHelperEditTab.setContent(testHelperAnchorScrollPane);

        UI.setAnchorMargins(testNodeTabPane, 50.0, 0.0, 0.0, 0.0);

        // Add created tabs to main tab pane
        testNodeTabPane.getTabs().addAll(testRawEditTab);
        testNodeTabPane.getTabs().addAll(testHelperEditTab);
        testNodeTabPane.getTabs().addAll(runningTabs);
        anchorPane.getChildren().addAll(testNodeTabPane);

        return tab;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        // Test
        SavableAttribute testAttribute = SavableAttribute.create(SavableAttribute.class);
        testAttribute.init("Test", getTest().getText().getClass().getName(), getTest().getText(), this);
        savableAttributes.add(testAttribute);

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    // Handles and runs all text typed in
    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        // One Time Variable can be used to pass in a replacement Test object to run - for example one edited by an InputNode
        // If one time variable does not exist then it will fall back to the original test
        Test testToRun = null;
        if (nodeRunParams.getOneTimeVariable() != null) {
            if (nodeRunParams.getOneTimeVariable() instanceof Test) {
                testToRun = (Test) nodeRunParams.getOneTimeVariable();
            }
        } else {
            testToRun = test.cloneTest();
        }

        if (testToRun != null) {
            TestRunner testRunner = new TestRunner(testToRun, this.getProgram());
            testRunner.run();
        }
    }

    public Test applyInputs(InputNode inputNode) {
        Test editedTest = new Test(this);
        editedTest.setText(test.getText());
        return editedTest.applyInputs(inputNode);
    }

    public String getAceTextAreaText() {
        return getTest().getText();
    }

    public void setAceTextAreaText(String scriptText) {
        getTest().setText(scriptText);
    }

    public Test cloneTest() {
        Test cloneTest = Test.create(Test.class);
        cloneTest.setParentTestNode(this);
        cloneTest.setText(test.getText());
        cloneTest.setClone(true);
        cloneTest.save();
        return cloneTest;
    }

    public Test getTest() {
        if (this.test == null) {
            this.test = new Test(this);
        }
        return test;
    }

    public void setTest(String testString) {
        if (this.test == null) {
            this.test = new Test(this);
        }
        this.test.setText(testString);
    }
}
