package application.node;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.AceTextArea;
import application.gui.Controller;
import application.gui.Source;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SourceNode extends DrawableNode {
    private Source source = null;
    private Color fillColour = Color.LIGHTGREEN;

    public SourceNode(SourceNode sourceNode) {
        this.source = new Source(this);

        this.setId(-1);
        this.setX(sourceNode.getX());
        this.setY(sourceNode.getY());
        this.setWidth(sourceNode.getWidth());
        this.setHeight(sourceNode.getHeight());
        this.setColor(sourceNode.getColor());
        this.setScale(sourceNode.getScale());
        super.setContainedText(sourceNode.getContainedText());
        this.setProgramId(sourceNode.getProgramId());
        this.setNextNodeToRun(sourceNode.getNextNodeToRun());

        this.setSource(sourceNode.getSource().getSource());
    }

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
    }

    public void setId(Integer id) {
        super.setId(id);
        this.source.setId(id);
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.add(new SavableAttribute("Source", getSource().getSource().getClass().getName(), getSource().getSource()));
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

    public String getAceTextAreaText() {
        return getSource().getSource();
    }

    public void setAceTextAreaText(String sourceText) {
        getSource().setSource(sourceText);
    }

    public void run() {
        this.getSource().run(true, new HashMap<>());
    }

    @Override
    public Color getFillColour() {
        return fillColour;
    }

    @Override
    public void setFillColour(Color fillColour) {
        this.fillColour = fillColour;
    }
}
