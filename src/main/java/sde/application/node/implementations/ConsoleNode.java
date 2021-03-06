package sde.application.node.implementations;

import sde.application.gui.Controller;
import sde.application.gui.Program;
import sde.application.gui.UI;
import sde.application.node.design.DrawableNode;
import sde.application.node.objects.Trigger;
import sde.application.utils.NodeRunParams;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;

/**
 * This node displays a console that can be written to.  It also allows other nodes to run triggers on it which
 * will read the output and react when coded to do so.
 */
public class ConsoleNode extends DrawableNode {
    private TextArea consoleTextArea = new TextArea();

    /**
     * @param consoleNode
     */
    public ConsoleNode(ConsoleNode consoleNode) {
        this.setX(consoleNode.getX());
        this.setY(consoleNode.getY());
        this.setWidth(consoleNode.getWidth());
        this.setHeight(consoleNode.getHeight());
        this.setColor(consoleNode.getColor());
        this.setScale(consoleNode.getScale());
        this.setContainedText(consoleNode.getContainedText());
//        this.setProgramUuid(consoleNode.getProgramUuid());
        this.setNextNodeToRun(consoleNode.getNextNodeToRun());
    }

    public ConsoleNode() {
        super();
    }

    /**
     * @return
     */
    public List<String> getAvailableTriggers() {
        List<String> triggers = new ArrayList<>();

        triggers.add("New line");

        return triggers;
    }

    /**
     * @return
     */
    public List<String> getAvailableTriggerActions() {
        List<String> triggers = new ArrayList<>();

        triggers.add("Send line on..");

        return triggers;
    }

    private String consoleToWrite = "";

    /**
     * Writes some text and then adds a new line character to the end of it.
     *
     * @param text The message we want to write to the console.
     */
    public void writeLineToConsole(String text) {
        writeToConsole(text + "\n\r");
    }

    /**
     * Writes some text directly to the console as passed in.
     *
     * @param text The message we want to write to the console.
     */
    public void writeToConsole(String text) {
        consoleToWrite += text;

        class ConsoleNodeWriteConsole implements Runnable {
            public void run() {
                if (consoleTextArea != null) {
                    consoleTextArea.appendText(consoleToWrite);

                    List<Trigger> triggers = getProgram().getFlowController().getActiveTriggers(getContainedText(), "New line");
                    for (Trigger trigger : triggers) {
                        NodeRunParams nodeRunParams = new NodeRunParams();
                        nodeRunParams.setOneTimeVariable(consoleToWrite);
                        Program.runHelper(trigger.getParent().getNextNodeToRun(), getProgram().getFlowController().getReferenceID(), trigger.getParent(), false, true, null, nodeRunParams);
                    }

                    consoleToWrite = "";
                }
            }
        }

        Platform.runLater(new ConsoleNodeWriteConsole());
    }

    /**
     *
     */
    public void clearConsole() {
        consoleTextArea.clear();
    }

    /**
     * @return
     */
    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        UI.setAnchorMargins(consoleTextArea, 50.0, 11.0, 11.0, 11.0);

        anchorPane.getChildren().add(consoleTextArea);

        MenuItem menuItemNewProgram = new MenuItem("Clear All");
        menuItemNewProgram.setOnAction(event -> clearConsole());

        ContextMenu clearTextAreaContextMenu = new ContextMenu();
        clearTextAreaContextMenu.getItems().add(menuItemNewProgram);

        consoleTextArea.setContextMenu(clearTextAreaContextMenu);

        return tab;
    }
}
