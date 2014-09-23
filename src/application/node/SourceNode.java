package application.node;

import application.gui.FlowController;
import application.gui.Source;
import application.data.SavableAttribute;
import application.data.DataBank;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SourceNode extends DrawableNode {
    private Source source = null;
    private FlowController parentController;

    public SourceNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public SourceNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        this.source = new Source(this);
    }

    public SourceNode(Double x, Double y, String containedText, String source, Integer id, Integer programId) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, programId, id);
        this.source = new Source(this, source, id);
    }

    public void setContainedText(String containedText) {
        super.setContainedText(containedText);
        DataBank.saveNode(this);
    }

    public void setId(Integer id) {
        super.setId(id);
        this.source.setId(id);
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<SavableAttribute>();

        savableAttributes.add(new SavableAttribute("Source", source.getSource().getClass().getName(), source.getSource()));
        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public String getNodeType() {
        return "SourceNode";
    }

    public void setSource(String sourceString) {
        if (source == null) {
            source = new Source(this);
        }
        source.setSource(sourceString);
    }

    public Source getSource() {
        if (this.source == null) {
            this.source = new Source(this);
        }
        return this.source;
    }

    public void run() {
        this.source.run(false, new HashMap<String, Object>());
    }
}
