package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.AceTextArea;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.node.objects.Batch;
import application.node.objects.BatchTest;
import application.node.objects.Input;
import application.utils.NodeRunParams;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The purpose of this class is to handle the script for a Batch process run on a Windows machine.
 * To use this node we need to pass this to a configured {@link application.node.implementations.WindowsNode} run method
 * as the 'oneTimeVariable', this will then be run.
 *
 * @author Alex Brown
 */
public class BatchNode extends DrawableNode {
    private Batch batch = null;

    /**
     * This method is used to copy a {@link application.node.implementations.BatchNode} and give back a new object.
     *
     * @param batchNode The {@link application.node.implementations.BatchNode} we want to create a copy of.
     */
    public BatchNode(BatchNode batchNode) {
        this.setX(batchNode.getX());
        this.setY(batchNode.getY());
        this.setWidth(batchNode.getWidth());
        this.setHeight(batchNode.getHeight());
        this.setColor(batchNode.getColor());
        this.setScale(batchNode.getScale());
        this.setContainedText(batchNode.getContainedText());
//        this.setProgramUuid(batchNode.getProgramUuid());
        this.setNextNodeToRun(batchNode.getNextNodeToRun());

        this.batch = new Batch(this);
        this.setBatch(batchNode.getBatch().getScript());
    }

    public BatchNode() {
        super();
        super.setColor(Color.BLACK);
    }

    /**
     * This method is intended to handle the creation of the GUI required to interact with this node by the user.
     *
     * @return A fully built interface for this node embedded in a {@link javafx.scene.control.Tab}.
     */
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

    /**
     * This method will return any additional values we want to save specific to this node as a list of {@link application.data.SavableAttribute}.
     *
     * @return All data we want to save that is specific to this node.
     */
    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        // Batch
        SavableAttribute batchAttribute = SavableAttribute.create(SavableAttribute.class);
        batchAttribute.init("Batch", getBatch().getScript().getClass().getName(), getBatch().getScript(), this);
        savableAttributes.add(batchAttribute);

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    /**
     * This run method has no content and is not intended to be run.  {@link application.node.implementations.BatchNode} objects.
     * Should be passed into the run method of a {@link application.node.implementations.WindowsNode} via the
     * NodeRunParams object.
     *
     * @param whileWaiting  Flag to tell run method not to return until thread has finished executing.
     * @param nodeRunParams Holds objects that can be passed between run methods of {@link application.node.design.DrawableNode} objects.
     */
    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
    }

    /**
     * @param batchString Sets value of internal {@link application.node.objects.Batch} object or creates one if it doesn't exist.
     */
    public void setBatch(String batchString) {
        if (this.batch == null) {
            this.batch = new Batch(this);
        }
        this.batch.setScript(batchString);
    }

    /**
     * @param inputNode The {@link application.node.implementations.InputNode} that we want to apply all applicable
     *                  values of to this {@link application.node.implementations.BatchNode}.
     * @return Returns a newly created {@link application.node.objects.BatchTest} with the applied inputs.
     * This is not saved beyond this method return and so needs to be passed to a  {@link application.node.implementations.WindowsNode}.
     */
    public BatchTest applyInputs(InputNode inputNode) {
        BatchTest editedTest = new BatchTest(this);
        editedTest.setText(getBatch().getScript());
        for (Input input : inputNode.getInputs()) {
            if (!input.getVariableName().isEmpty() && !input.getVariableValue().isEmpty()) {
                editedTest.setText(editedTest.getText().replaceAll(Pattern.quote(input.getVariableName()), input.getVariableValue()));
            }
        }

        return editedTest;
    }

    /**
     * @return Returns {@link java.lang.String} representation of batch script.
     */
    public String getAceTextAreaText() {
        return getBatch().getScript();
    }

    /**
     * @param scriptText Sets the text directly of the internal {@link application.node.objects.Batch} object.
     */
    public void setAceTextAreaText(String scriptText) {
        getBatch().setScript(scriptText);
    }

    /**
     * @return Returns internal {@link application.node.objects.Batch} object or creates one if it doesn't exist.
     */
    public Batch getBatch() {
        if (this.batch == null) {
            this.batch = new Batch(this);
        }
        return this.batch;
    }
}
