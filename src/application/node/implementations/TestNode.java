package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.AceTextArea;
import application.gui.Controller;
import application.net.proxy.WebProxy;
import application.node.design.DrawableNode;
import application.node.objects.Input;
import application.node.objects.Test;
import application.test.TestCommand;
import application.test.TestResult;
import application.test.action.ActionControl;
import application.utils.BrowserHelper;
import application.utils.NodeRunParams;
import application.utils.SDEThread;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class TestNode extends DrawableNode {
    private Test test;
    private AceTextArea aceTextArea = null;

    // This will make a copy of the node passed to it
    public TestNode(TestNode testNode) {
        this.setId(-1);
        this.setX(testNode.getX());
        this.setY(testNode.getY());
        this.setY(testNode.getY());
        this.setWidth(testNode.getWidth());
        this.setHeight(testNode.getHeight());
        this.setColor(testNode.getColor());
        this.setScale(testNode.getScale());
        this.setContainedText(testNode.getContainedText());
        this.setProgramId(testNode.getProgramId());
        this.setNextNodeToRun(testNode.getNextNodeToRun());

        this.test = new Test(this);
        this.setTest(testNode.getTest().getText());
    }

    public TestNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public TestNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        this.test = new Test(this);
    }

    public TestNode(Double x, Double y, Double width, Double height, Color color, String containedText, Integer programId, Integer id) {
        super(x, y, width, height, color, containedText, programId, id);
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        VBox vBox = new VBox(5);

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();
        Button recordButton = AwesomeDude.createIconButton(AwesomeIcon.DOT_CIRCLE_ALT);

        recordButton.setPrefWidth(35);
        recordButton.setTooltip(new Tooltip("Record from browser"));
        recordButton.setId("recordButton-" + getId());
        recordButton.setOnAction(event -> {
            WebDriver driver = BrowserHelper.getChrome();
            driver.get("http://jboss-alex:8080/spl/focal/Login");
        });

        aceTextArea = new AceTextArea(this, "ace/mode/text");

        vBox.getChildren().add(recordButton);
        vBox.getChildren().add(aceTextArea);

        AnchorPane.setBottomAnchor(vBox, 0.0);
        AnchorPane.setLeftAnchor(vBox, 11.0);
        AnchorPane.setRightAnchor(vBox, 0.0);
        AnchorPane.setTopAnchor(vBox, 50.0);

        anchorPane.getChildren().add(vBox);

        return tab;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.add(new SavableAttribute("Test", getTest().getText().getClass().getName(), getTest().getText()));
        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }


    // Handles and runs all text typed in
    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        // One Time Variable can be used to pass in a replacement Test object to run - for example one edited by an InputNode
        // If one time variable does not exist then it will fall back to the original test
        Test testToRun = test;
        if (nodeRunParams.getOneTimeVariable() != null) {
            if (nodeRunParams.getOneTimeVariable() instanceof Test) {
                testToRun = (Test) nodeRunParams.getOneTimeVariable();
            }
        }

        if (testToRun != null) {
            TestResult testResult = DataBank.createNewTestResult();

            List<String> commands = new ArrayList<>();
            Collections.addAll(commands, testToRun.getText().split("[\\r\\n]+"));

            WebDriver driver = BrowserHelper.getChrome();

            Integer lineCounter = 1;

            // Creates the WebProxy used for this node
            WebProxy webProxy = new WebProxy();
            SDEThread webProxyThread = new SDEThread(webProxy);

            for (String command : commands) {
                TestCommand testCommand = TestCommand.parseCommand(command);

                System.out.println("Command " + command);

                // If the user is viewing the node at the time we can select the line that is currently being run
                if (aceTextArea != null) {
                    aceTextArea.goToLine(lineCounter);
                }
                lineCounter++;

                // Here we are retrieving the correct class held within ActionControl mapping (within application.test.action)
                // and initialising the object and performing the required action which is then handled by the object
                try {
                    Class actionClass = ActionControl.getClassMapping(testCommand.getMainCommand());
                    ActionControl actionControl = (ActionControl) actionClass.getDeclaredConstructor().newInstance();
                    actionControl.initialise(webProxy, driver, testCommand, testResult);
                    actionControl.performAction();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            TestResult testResultReloaded = new TestResult();
            testResultReloaded.setId(testResult.getId());
            DataBank.loadTestSteps(testResultReloaded);

            // Doing something with the screenshots
//            Integer counter = 1;
//            for (TestStep testStep : testResultReloaded.getTestSteps()) {
//                try {
//                    ImageIO.write(testStep.getScreenshot(), "png", new File("C:\\Users\\alex\\Desktop\\TestStep" + testResultReloaded.getId() + "-" + counter + ".png"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                counter++;
//            }
        }
    }

    public Test applyInputs(InputNode inputNode) {
        Test editedTest = new Test(this);
        editedTest.setText(getTest().getText());
        for (Input input : inputNode.getInputs()) {
            if (!input.getVariableName().isEmpty() && !input.getVariableValue().isEmpty()) {
                editedTest.setText(editedTest.getText().replaceAll(Pattern.quote(input.getVariableName()), input.getVariableValue()));
            }
        }

        return editedTest;
    }

    public String getAceTextAreaText() {
        return getTest().getText();
    }

    public void setAceTextAreaText(String scriptText) {
        getTest().setText(scriptText);
    }

    public Test getTest() {
        return test;
    }

    public void setTest(String testString) {
        if (this.test == null) {
            this.test = new Test(this);
        }
        this.test.setText(testString);
    }
}
