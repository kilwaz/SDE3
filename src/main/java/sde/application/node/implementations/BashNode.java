package sde.application.node.implementations;

import sde.application.data.SavableAttribute;
import sde.application.gui.AceTextArea;
import sde.application.gui.Controller;
import sde.application.gui.UI;
import sde.application.node.design.DrawableNode;
import sde.application.node.objects.Bash;
import sde.application.node.objects.Input;
import sde.application.utils.NodeRunParams;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BashNode extends DrawableNode {
    private Bash bash = null;

    private static Logger log = Logger.getLogger(BashNode.class);

    // This will make a copy of the node passed to it
    public BashNode(BashNode bashNode) {
        this.setX(bashNode.getX());
        this.setY(bashNode.getY());
        this.setWidth(bashNode.getWidth());
        this.setHeight(bashNode.getHeight());
        this.setColor(bashNode.getColor());
        this.setScale(bashNode.getScale());
        this.setContainedText(bashNode.getContainedText());
//        this.setProgramUuid(bashNode.getProgramUuid());
        this.setNextNodeToRun(bashNode.getNextNodeToRun());

        this.bash = new Bash(this);
        this.setBash(bashNode.getBash().getScript());
    }

    public BashNode() {
        super();
        super.setColor(Color.BLACK);
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this, false);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        AceTextArea aceTextArea = new AceTextArea(this, "ace/mode/batch");

        UI.setAnchorMargins(aceTextArea, 50.0, 0.0, 11.0, 0.0);

        anchorPane.getChildren().add(aceTextArea);

        return tab;
    }

    public void applyInputs(InputNode inputNode) {
        for (Input input : inputNode.getInputs()) {
            if (!input.getVariableName().isEmpty() && !input.getVariableValue().isEmpty()) {
                String script = getBash().getScript();
                getBash().setScript(script.replaceAll(Pattern.quote(input.getVariableName()), input.getVariableValue().replace("$", "\\$")));
            }
        }
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        // Bash
        SavableAttribute bashAttribute = SavableAttribute.create(SavableAttribute.class);
        bashAttribute.init("Bash", getBash().getScript().getClass().getName(), getBash().getScript(), this);
        savableAttributes.add(bashAttribute);

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
    }

    public void setBash(String bashString) {
        if (this.bash == null) {
            this.bash = new Bash(this);
        }
        if (bashString == null) {
            bashString = "";
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
