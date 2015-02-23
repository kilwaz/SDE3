package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.AceTextArea;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.node.objects.Input;
import application.node.objects.Switch;
import application.node.objects.Test;
import application.test.TestCommand;
import application.test.TestParameter;
import application.utils.BrowserHelper;
import application.utils.NodeRunParams;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        aceTextArea = new AceTextArea(this, "ace/mode/text");

        AnchorPane.setBottomAnchor(aceTextArea, 0.0);
        AnchorPane.setLeftAnchor(aceTextArea, 11.0);
        AnchorPane.setRightAnchor(aceTextArea, 0.0);
        AnchorPane.setTopAnchor(aceTextArea, 50.0);

        anchorPane.getChildren().add(aceTextArea);

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
            List<String> commands = new ArrayList<>();

            Collections.addAll(commands, testToRun.getText().split("[\\r\\n]+"));

            WebDriver driver = BrowserHelper.getChrome();

            Integer lineCounter = 1;

            for (String command : commands) {
                TestCommand testCommand = TestCommand.parseCommand(command);

                // If the user is viewing the node at the time we can select the line that is currently being run
                if (aceTextArea != null) {
                    aceTextArea.goToLine(lineCounter);
                }
                lineCounter++;

                if ("go".equals(testCommand.getMainCommand())) {
                    TestParameter url = testCommand.getParameterByName("url");

                    if (url != null) { // Go to the url specified
                        driver.get(url.getParameterValue());
                    }
                } else if ("input".equals(testCommand.getMainCommand())) { // Input a value into an element
                    TestParameter elementId = testCommand.getParameterByName("id");
                    TestParameter valueToEnter = testCommand.getParameterByPath("value");
                    if (elementId != null && valueToEnter != null) {
                        WebElement element = driver.findElement(By.id(elementId.getParameterValue()));
                        if (element != null) {
                            element.sendKeys(valueToEnter.getParameterValue());
                        }
                    }
                } else if ("click".equals(testCommand.getMainCommand())) { // Click on an element
                    TestParameter elementId = testCommand.getParameterByName("id");
                    if (elementId != null) {
                        WebElement element = driver.findElement(By.id(elementId.getParameterValue()));
                        if (element != null) {
                            element.click();
                        }
                    }
                } else if ("wait".equals(testCommand.getMainCommand())) { // Wait for condition to be true
                    TestParameter elementToBeClickable = testCommand.getParameterByPath("clickable::id");
                    TestParameter elementToBePresent = testCommand.getParameterByPath("presence::id");
                    if (elementToBeClickable != null) { // If it is specified wait until this element is clickable
                        WebDriverWait wait = new WebDriverWait(driver, 10);
                        wait.until(ExpectedConditions.elementToBeClickable(By.id(elementToBeClickable.getParameterValue())));
                    }
                    if (elementToBePresent != null) { // If it is specified wait until this element is clickable
                        WebDriverWait wait = new WebDriverWait(driver, 10);
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(elementToBePresent.getParameterValue())));
                    }
                } else if ("frame".equals(testCommand.getMainCommand())) { // Wait for condition to be true
                    TestParameter frameToSelectById = testCommand.getParameterByPath("id");
                    TestParameter frameToSelect = testCommand.getParameterByPath("select");

                    if (frameToSelectById != null) {
                        driver.switchTo().frame(frameToSelectById.getParameterValue());
                    }

                    if (frameToSelect != null) {
                        driver.switchTo().defaultContent();
                    }
                } else if ("exit".equals(testCommand.getMainCommand())) { // Closes down the browser
                    driver.close();
                    driver.quit();
                }
            }
        }
    }

    public Test applyInputs(InputNode inputNode) {
        Test editedTest = new Test(this);
        for (Input input : inputNode.getInputs()) {
            if (!input.getVariableName().isEmpty() && !input.getVariableValue().isEmpty()) {
                String test = getTest().getText();
                editedTest.setText(test.replaceAll(Pattern.quote(input.getVariableName()), input.getVariableValue()));
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
