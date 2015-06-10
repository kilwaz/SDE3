package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.utils.CustomObject;
import application.utils.NodeRunParams;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomObjectNode extends DrawableNode {
    private HashMap<String, CustomObject> customObjectHashMap = new HashMap<>();

    // This will make a copy of the node passed to it
    public CustomObjectNode(CustomObjectNode customObjectNode) {
        this.setId(-1);
        this.setX(customObjectNode.getX());
        this.setY(customObjectNode.getY());
        this.setWidth(customObjectNode.getWidth());
        this.setHeight(customObjectNode.getHeight());
        this.setColor(customObjectNode.getColor());
        this.setScale(customObjectNode.getScale());
        this.setContainedText(customObjectNode.getContainedText());
        this.setProgramId(customObjectNode.getProgramId());
        this.setNextNodeToRun(customObjectNode.getNextNodeToRun());
    }

    public CustomObjectNode(Integer id, Integer programId) {
        super(id, programId);
        DataBank.loadCustomObjects(this);
    }

    public CustomObjectNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        DataBank.loadCustomObjects(this);
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());
        customObjectHashMap.values().forEach(DataBank::saveCustomObject);

        return savableAttributes;
    }

    public void addCustomObject(Object payload, String payloadReference) {
        if (customObjectHashMap.get(payloadReference) != null) {
            customObjectHashMap.get(payloadReference).setPayload(payload);
            DataBank.saveCustomObject(customObjectHashMap.get(payloadReference));
        } else {
            DataBank.createNewCustomObject(payload, payloadReference, this);
        }
    }

    public void addCustomObject(CustomObject customObject) {
        customObjectHashMap.put(customObject.getPayLoadReference(), customObject);
    }

    public void deleteCustomObject(String payloadReference) {
        if (payloadReference != null && customObjectHashMap.get(payloadReference) != null) {
            DataBank.deleteCustomObject(customObjectHashMap.remove(payloadReference));
        }
    }

    public void loadObjects() {
        DataBank.loadCustomObjects(this);
    }

    public void saveObjects() {
        customObjectHashMap.values().forEach(DataBank::saveCustomObject);
    }

    public CustomObject getCustomObject(String reference) {
        return customObjectHashMap.get(reference);
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {

    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        return tab;
    }
}
