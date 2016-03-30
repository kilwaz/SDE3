package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.utils.CustomObject;
import application.utils.NodeRunParams;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CustomObjectNode extends DrawableNode {
    private ConcurrentHashMap<String, CustomObject> customObjectHashMap = new ConcurrentHashMap<>();

    // This will make a copy of the node passed to it
    public CustomObjectNode(CustomObjectNode customObjectNode) {
        this.setX(customObjectNode.getX());
        this.setY(customObjectNode.getY());
        this.setWidth(customObjectNode.getWidth());
        this.setHeight(customObjectNode.getHeight());
        this.setColor(customObjectNode.getColor());
        this.setScale(customObjectNode.getScale());
        this.setContainedText(customObjectNode.getContainedText());
//        this.setProgramUuid(customObjectNode.getProgramUuid());
        this.setNextNodeToRun(customObjectNode.getNextNodeToRun());
    }

    public CustomObjectNode() {
        super();
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());
        customObjectHashMap.values().forEach(CustomObject::save);

        return savableAttributes;
    }

    public void addCustomObject(Object payload, String payloadReference) {
        if (customObjectHashMap.get(payloadReference) != null) {
            customObjectHashMap.get(payloadReference).setPayload(payload);
            customObjectHashMap.get(payloadReference).save();
        } else {
            CustomObject customObject = CustomObject.create(CustomObject.class);
            customObject.setPayload(payload);
            customObject.setPayLoadReference(payloadReference);
            customObject.setParent(this);
            customObject.save();
            customObjectHashMap.put(customObject.getPayLoadReference(), customObject);
        }
    }

    public void addCustomObject(CustomObject customObject) {
        customObjectHashMap.put(customObject.getPayLoadReference(), customObject);
    }

    public void deleteCustomObject(String payloadReference) {
        if (payloadReference != null && customObjectHashMap.get(payloadReference) != null) {
            customObjectHashMap.remove(payloadReference).save();
        }
    }

    public void loadObjects() {
        //CustomObject customObject = new CustomObject();

        DataBank.loadCustomObjects(this);
    }

    public void saveObjects() {
        customObjectHashMap.values().forEach(CustomObject::save);
    }

    public CustomObject getCustomObject(String reference) {
        return customObjectHashMap.get(reference);
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {

    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        return tab;
    }
}
