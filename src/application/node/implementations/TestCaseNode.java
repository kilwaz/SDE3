package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.AceTextArea;
import application.gui.Controller;
import application.gui.UI;
import application.node.design.DrawableNode;
import application.node.objects.Logic;
import application.test.core.TestCase;
import application.utils.NodeRunParams;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class TestCaseNode extends DrawableNode {
    private static Logger log = Logger.getLogger(TestCaseNode.class);
    private Logic logic = null;

    public TestCaseNode(TestCaseNode testCaseNode) {
        this.logic = new Logic(this);

        this.setX(testCaseNode.getX());
        this.setY(testCaseNode.getY());
        this.setWidth(testCaseNode.getWidth());
        this.setHeight(testCaseNode.getHeight());
        this.setColor(testCaseNode.getColor());
        this.setScale(testCaseNode.getScale());
        super.setContainedText(testCaseNode.getContainedText());
        this.setNextNodeToRun(testCaseNode.getNextNodeToRun());

        this.setLogic(testCaseNode.getLogic().getLogic());
    }

    public TestCaseNode() {
        super();
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        // Logic
        SavableAttribute logicAttribute = SavableAttribute.create(SavableAttribute.class);
        logicAttribute.init("Logic", getLogic().getLogic().getClass().getName(), getLogic().getLogic(), this);
        savableAttributes.add(logicAttribute);

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        AceTextArea aceTextArea = new AceTextArea(this, "ace/mode/java");

        UI.setAnchorMargins(aceTextArea, 50.0, 0.0, 11.0, 0.0);

        anchorPane.getChildren().add(aceTextArea);

        return tab;
    }

    public Logic getLogic() {
        if (this.logic == null) {
            this.logic = new Logic(this);
        }
        return this.logic;
    }

    public void setLogic(String logicString) {
        if (logic == null) {
            logic = new Logic(this);
        }
        logic.setLogic(logicString);
    }

    public String getAceTextAreaText() {
        return getLogic().getLogic();
    }

    public void setAceTextAreaText(String logicText) {
        getLogic().setLogic(logicText);
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        TestCase testCase = (TestCase) this.getLogic().getObjectInstance();
        testCase.setParentNode(this);
        testCase.init();
    }
}
