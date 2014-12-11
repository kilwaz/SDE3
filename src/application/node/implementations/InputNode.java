package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class InputNode extends DrawableNode {

    // This will make a copy of the node passed to it
    public InputNode(InputNode inputNode) {
        this.setId(-1);
        this.setX(inputNode.getX());
        this.setY(inputNode.getY());
        this.setWidth(inputNode.getWidth());
        this.setHeight(inputNode.getHeight());
        this.setColor(inputNode.getColor());
        this.setScale(inputNode.getScale());
        this.setContainedText(inputNode.getContainedText());
        this.setProgramId(inputNode.getProgramId());
        this.setNextNodeToRun(inputNode.getNextNodeToRun());
    }

    public InputNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public InputNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
    }

    public InputNode(Double x, Double y, Double width, Double height, Color color, String containedText, Integer programId, Integer id) {
        super(x, y, width, height, color, containedText, programId, id);
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);

        return tab;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<SavableAttribute>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }
}
