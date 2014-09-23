package application.node;

import application.FlowController;
import application.Switch;
import application.data.SavableAttribute;
import application.data.DataBank;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SwitchNode extends DrawableNode {
    private List<Switch> aSwitches = new ArrayList<Switch>();

    public SwitchNode(Integer id, Integer programId) {
        super(id, programId);
        DataBank.loadSplits(this);
        if (aSwitches.size() < 4) {
            DataBank.createNewSplit("target", this, true);
        }
    }

    public SwitchNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        DataBank.loadSplits(this);
    }

    public void updateSplitTarget(Integer splitId, String target) {
        for (Switch aSwitch : aSwitches) {
            if (aSwitch.getId().equals(splitId)) {
                aSwitch.setTarget(target);
                DataBank.saveSplit(aSwitch);
            }
        }

    }

    public void updateSplitEnabled(Integer splitId, Boolean enabled) {
        for (Switch aSwitch : aSwitches) {
            if (aSwitch.getId().equals(splitId)) {
                aSwitch.setEnabled(enabled);
                DataBank.saveSplit(aSwitch);
            }
        }
    }

    public void setSwitches(List<Switch> aSwitches) {
        this.aSwitches = aSwitches;
    }

    public List<Switch> getSwitches() {
        return this.aSwitches;
    }

    public Switch getSwitch(Integer id) {
        for (Switch aSwitch : aSwitches) {
            if (aSwitch.getId().equals(id)) {
                return aSwitch;
            }
        }

        return null;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<SavableAttribute>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void addSplit(Switch aSwitch) {
        aSwitches.add(aSwitch);
    }

    public void run(Boolean whileWaiting, HashMap<String, Object> map) {
        for (Switch aSwitch : aSwitches) {
            if (aSwitch.isEnabled()) {
                SourceNode sourceNode = FlowController.getSourceFromContainedText(aSwitch.getTarget());
                sourceNode.getSource().run(whileWaiting, map);
            }
        }
    }

    public String getNodeType() {
        return "SwitchNode";
    }
}
