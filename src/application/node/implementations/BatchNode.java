package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.AceTextArea;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.node.objects.Batch;
import application.node.objects.Input;
import application.utils.NodeRunParams;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BatchNode extends DrawableNode {
    private Batch batch = null;

    // This will make a copy of the node passed to it
    public BatchNode(BatchNode batchNode) {
        this.setId(-1);
        this.setX(batchNode.getX());
        this.setY(batchNode.getY());
        this.setWidth(batchNode.getWidth());
        this.setHeight(batchNode.getHeight());
        this.setColor(batchNode.getColor());
        this.setScale(batchNode.getScale());
        this.setContainedText(batchNode.getContainedText());
        this.setProgramId(batchNode.getProgramId());
        this.setNextNodeToRun(batchNode.getNextNodeToRun());


        this.batch = new Batch(this);
        this.setBatch(batchNode.getBatch().getScript());
    }

    public BatchNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public BatchNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        this.batch = new Batch(this);
    }

    public BatchNode(Double x, Double y, Double width, Double height, Color color, String containedText, Integer programId, Integer id) {
        super(x, y, width, height, color, containedText, programId, id);
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        AceTextArea aceTextArea = new AceTextArea(this, "ace/mode/text");

        AnchorPane.setBottomAnchor(aceTextArea, 0.0);
        AnchorPane.setLeftAnchor(aceTextArea, 11.0);
        AnchorPane.setRightAnchor(aceTextArea, 0.0);
        AnchorPane.setTopAnchor(aceTextArea, 50.0);

        anchorPane.getChildren().add(aceTextArea);

        return tab;
    }

    public void applyInputs(InputNode inputNode) {
        for (Input input : inputNode.getInputs()) {
            if (!input.getVariableName().isEmpty() && !input.getVariableValue().isEmpty()) {
                String script = getBatch().getScript();
                getBatch().setScript(script.replaceAll(Pattern.quote(input.getVariableName()), input.getVariableValue()));
            }
        }
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.add(new SavableAttribute("Batch", getBatch().getScript().getClass().getName(), getBatch().getScript()));
        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
    }

    public void setBatch(String batchString) {
        if (this.batch == null) {
            this.batch = new Batch(this);
        }
        this.batch.setScript(batchString);
    }

    public String getAceTextAreaText() {
        return getBatch().getScript();
    }

    public void setAceTextAreaText(String scriptText) {
        getBatch().setScript(scriptText);
    }

    public Batch getBatch() {
        if (this.batch == null) {
            this.batch = new Batch(this);
        }
        return this.batch;
    }
}
