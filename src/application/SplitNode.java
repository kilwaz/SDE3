package application;

import application.utils.DataBank;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SplitNode extends DrawableNode {
    private List<Split> splits = new ArrayList<Split>();

    public SplitNode(Integer id, Integer programId) {
        super(id, programId);
        DataBank.loadSplits(this);
        if (splits.size() < 4) {
            DataBank.createNewSplit("target", this, true);
        }
    }

    public SplitNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        DataBank.loadSplits(this);
    }

    public void updateSplitTarget(Integer splitId, String target) {
        for (Split split : splits) {
            if (split.getId().equals(splitId)) {
                split.setTarget(target);
                DataBank.saveSplit(split);
            }
        }

    }

    public void updateSplitEnabled(Integer splitId, Boolean enabled) {
        for (Split split : splits) {
            if (split.getId().equals(splitId)) {
                split.setEnabled(enabled);
                DataBank.saveSplit(split);
            }
        }
    }

    public void setSplits(List<Split> splits) {
        this.splits = splits;
    }

    public List<Split> getSplits() {
        return this.splits;
    }

    public Split getSplit(Integer id) {
        for (Split split : splits) {
            if (split.getId().equals(id)) {
                return split;
            }
        }

        return null;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<SavableAttribute>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void addSplit(Split split) {
        splits.add(split);
    }

    public void run(Boolean whileWaiting, HashMap<String, Object> map) {
        for (Split split : splits) {
            if (split.isEnabled()) {
                SourceNode sourceNode = FlowController.getSourceFromContainedText(split.getTarget());
                sourceNode.getSource().run(whileWaiting, map);
            }
        }
    }

    public String getNodeType() {
        return "SplitNode";
    }
}
