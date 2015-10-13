package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.node.objects.SDEFile;
import application.utils.NodeRunParams;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileStoreNode extends DrawableNode {
    private HashMap<Integer, SDEFile> sdeFileHashMap = new HashMap<>();

    // This will make a copy of the node passed to it
    public FileStoreNode(FileStoreNode fileStoreNode) {
        this.setId(-1);
        this.setX(fileStoreNode.getX());
        this.setY(fileStoreNode.getY());
        this.setWidth(fileStoreNode.getWidth());
        this.setHeight(fileStoreNode.getHeight());
        this.setColor(fileStoreNode.getColor());
        this.setScale(fileStoreNode.getScale());
        this.setContainedText(fileStoreNode.getContainedText());
        this.setProgramId(fileStoreNode.getProgramId());
        this.setNextNodeToRun(fileStoreNode.getNextNodeToRun());
    }

    public FileStoreNode(Integer id, Integer programId) {
        super(id, programId);
        DataBank.loadSDEFile(this);
    }

    public FileStoreNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        DataBank.loadSDEFile(this);
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());
        sdeFileHashMap.values().forEach(DataBank::saveSDEFile);

        return savableAttributes;
    }

    public void addSDEFile(File file, String payloadReference) {
        if (sdeFileHashMap.get(payloadReference) != null) {
            sdeFileHashMap.get(payloadReference).setFile(file);
            DataBank.saveSDEFile(sdeFileHashMap.get(payloadReference));
        } else {
            DataBank.createNewSDEFile(file, this);
        }
    }

    public void addSDEFile(SDEFile sdeFile) {
        sdeFileHashMap.put(sdeFile.getId(), sdeFile);
    }

    public void deleteSDEFile(String payloadReference) {
        if (payloadReference != null && sdeFileHashMap.get(payloadReference) != null) {
            DataBank.deleteSDEFile(sdeFileHashMap.remove(payloadReference));
        }
    }

    public void loadObjects() {
        DataBank.loadSDEFile(this);
    }

    public void saveObjects() {
        sdeFileHashMap.values().forEach(DataBank::saveSDEFile);
    }

    public SDEFile getSDEFile(Integer reference) {
        return sdeFileHashMap.get(reference);
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
