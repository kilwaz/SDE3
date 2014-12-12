package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.AceTextArea;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.node.objects.Bash;
import application.node.objects.Input;
import application.utils.NodeRunParams;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BashNode extends DrawableNode {
    private Bash bash = null;

    // This will make a copy of the node passed to it
    public BashNode(BashNode bashNode) {
        this.setId(-1);
        this.setX(bashNode.getX());
        this.setY(bashNode.getY());
        this.setWidth(bashNode.getWidth());
        this.setHeight(bashNode.getHeight());
        this.setColor(bashNode.getColor());
        this.setScale(bashNode.getScale());
        this.setContainedText(bashNode.getContainedText());
        this.setProgramId(bashNode.getProgramId());
        this.setNextNodeToRun(bashNode.getNextNodeToRun());


        this.bash = new Bash(this);
        this.setBash(bashNode.getBash().getScript());
    }

    public BashNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public BashNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        this.bash = new Bash(this);
    }

    public BashNode(Double x, Double y, Double width, Double height, Color color, String containedText, Integer programId, Integer id) {
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
                String script = getBash().getScript();
                getBash().setScript(script.replaceAll(Pattern.quote(input.getVariableName()), input.getVariableValue()));
            }
        }
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.add(new SavableAttribute("Bash", getBash().getScript().getClass().getName(), getBash().getScript()));
        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        //System.out.println("Added the script to the map");
        //map.put("bashScript", getBash().getScript());
    }

    public void setBash(String bashString) {
        if (this.bash == null) {
            this.bash = new Bash(this);
        }
        this.bash.setScript(bashString);
    }

    public String getAceTextAreaText() {
        return getBash().getScript();
    }

    public void setAceTextAreaText(String scriptText) {
        getBash().setScript(scriptText);
    }

    public Bash getBash() {
        if (this.bash == null) {
            this.bash = new Bash(this);
        }
        return this.bash;
    }
}
