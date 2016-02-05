package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.node.objects.SDEFile;
import application.utils.NodeRunParams;
import javafx.scene.control.Tab;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileStoreNode extends DrawableNode {
    private HashMap<String, SDEFile> sdeFileHashMap = new HashMap<>();

    // This will make a copy of the node passed to it
    public FileStoreNode(FileStoreNode fileStoreNode) {
        this.setX(fileStoreNode.getX());
        this.setY(fileStoreNode.getY());
        this.setWidth(fileStoreNode.getWidth());
        this.setHeight(fileStoreNode.getHeight());
        this.setColor(fileStoreNode.getColor());
        this.setScale(fileStoreNode.getScale());
        this.setContainedText(fileStoreNode.getContainedText());
//        this.setProgramUuid(fileStoreNode.getProgramUuid());
        this.setNextNodeToRun(fileStoreNode.getNextNodeToRun());
    }

    public FileStoreNode() {
        super();
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());
        sdeFileHashMap.values().forEach(SDEFile::save);

        return savableAttributes;
    }

    public void addSDEFile(File file, String payloadReference) {
        if (sdeFileHashMap.get(payloadReference) != null) {
            sdeFileHashMap.get(payloadReference).setFile(file);
            sdeFileHashMap.get(payloadReference).save();
        } else {
            DataBank.createNewSDEFile(file, this);
        }
    }

    public void addSDEFile(SDEFile sdeFile) {
        sdeFileHashMap.put(sdeFile.getUuidString(), sdeFile);
    }

    public void deleteSDEFile(String payloadReference) {
        if (payloadReference != null && sdeFileHashMap.get(payloadReference) != null) {
            sdeFileHashMap.get(payloadReference).save();
        }
    }

    public void loadObjects() {
        DataBank.loadSDEFile(this);
    }

    public void saveObjects() {
        sdeFileHashMap.values().forEach(SDEFile::save);
    }

    public SDEFile getSDEFile(Integer reference) {
        return sdeFileHashMap.get(reference);
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {

    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        tab.getContent();

        return tab;
    }
}
