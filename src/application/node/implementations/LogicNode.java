package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.AceTextArea;
import application.gui.Controller;
import application.gui.Logic;
import application.node.design.DrawableNode;
import application.utils.NodeRunParams;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class LogicNode extends DrawableNode {
    private Logic logic = null;

    public LogicNode(LogicNode logicNode) {
        this.logic = new Logic(this);

        this.setId(-1);
        this.setX(logicNode.getX());
        this.setY(logicNode.getY());
        this.setWidth(logicNode.getWidth());
        this.setHeight(logicNode.getHeight());
        this.setColor(logicNode.getColor());
        this.setScale(logicNode.getScale());
        super.setContainedText(logicNode.getContainedText());
        this.setProgramId(logicNode.getProgramId());
        this.setNextNodeToRun(logicNode.getNextNodeToRun());

        this.setLogic(logicNode.getLogic().getLogic());
    }

    public LogicNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public LogicNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        this.logic = new Logic(this);
    }

    public LogicNode(Double x, Double y, String containedText, String logic, Integer id, Integer programId) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, programId, id);
        this.logic = new Logic(this, logic, id);
    }

    public void setContainedText(String containedText) {
        super.setContainedText(containedText);
    }

    public void setId(Integer id) {
        super.setId(id);
        this.logic.setId(id);
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.add(new SavableAttribute("Source", getLogic().getLogic().getClass().getName(), getLogic().getLogic()));
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
