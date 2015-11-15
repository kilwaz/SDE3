package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.AceTextArea;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.node.objects.Logic;
import application.utils.NodeRunParams;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LogicNode extends DrawableNode {
    private Logic logic = null;

    private static Logger log = Logger.getLogger(LogicNode.class);

    public LogicNode(LogicNode logicNode) {
        this.logic = new Logic(this);

        this.setX(logicNode.getX());
        this.setY(logicNode.getY());
        this.setWidth(logicNode.getWidth());
        this.setHeight(logicNode.getHeight());
        this.setColor(logicNode.getColor());
        this.setScale(logicNode.getScale());
        super.setContainedText(logicNode.getContainedText());
//        this.setProgramUuid(logicNode.getProgramUuid());
        this.setNextNodeToRun(logicNode.getNextNodeToRun());

        this.setLogic(logicNode.getLogic().getLogic());
    }

    public LogicNode() {
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

        AnchorPane.setBottomAnchor(aceTextArea, 0.0);
        AnchorPane.setLeftAnchor(aceTextArea, 11.0);
        AnchorPane.setRightAnchor(aceTextArea, 0.0);
        AnchorPane.setTopAnchor(aceTextArea, 50.0);

        anchorPane.getChildren().add(aceTextArea);

        return tab;
    }

    public void setLogic(String logicString) {
        if (logic == null) {
            logic = new Logic(this);
        }
        logic.setLogic(logicString);
    }

    public Logic getLogic() {
        if (this.logic == null) {
            this.logic = new Logic(this);
        }
        return this.logic;
    }

    public String getAceTextAreaText() {
        return getLogic().getLogic();
    }

    public void setAceTextAreaText(String logicText) {
        getLogic().setLogic(logicText);
    }

    public void run() {
        this.getLogic().run(true, new NodeRunParams());
    }
}
