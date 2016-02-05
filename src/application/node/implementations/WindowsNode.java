package application.node.implementations;

import application.data.SavableAttribute;
import application.error.Error;
import application.gui.Controller;
import application.gui.Program;
import application.gui.UI;
import application.node.design.DrawableNode;
import application.node.objects.BatchTest;
import application.node.objects.Trigger;
import application.utils.NodeRunParams;
import application.utils.SDEUtils;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WindowsNode extends DrawableNode {
    private TextArea consoleTextArea = new TextArea();
    private static Logger log = Logger.getLogger(WindowsNode.class);

    // This will make a copy of the node passed to it
    public WindowsNode(LinuxNode linuxNode) {
        this.setX(linuxNode.getX());
        this.setY(linuxNode.getY());
        this.setWidth(linuxNode.getWidth());
        this.setHeight(linuxNode.getHeight());
        this.setColor(linuxNode.getColor());
        this.setScale(linuxNode.getScale());
        this.setContainedText(linuxNode.getContainedText());
//        this.setProgramUuid(linuxNode.getProgramUuid());
        this.setNextNodeToRun(linuxNode.getNextNodeToRun());
    }

    public WindowsNode() {
        super();
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        UI.setAnchorMargins(rows, 50.0, 11.0, 11.0, 11.0);

        rows.getChildren().add(consoleTextArea);

        consoleTextArea.setPrefRowCount(100);

        anchorPane.getChildren().add(rows);

        MenuItem menuItemNewProgram = new MenuItem("Clear All");
        menuItemNewProgram.setOnAction(event -> clearConsole());

        ContextMenu clearTextAreaContextMenu = new ContextMenu();
        clearTextAreaContextMenu.getItems().add(menuItemNewProgram);

        consoleTextArea.setContextMenu(clearTextAreaContextMenu);

        return tab;
    }

    public List<String> getAvailableTriggers() {
        List<String> triggers = new ArrayList<>();

        triggers.add("New line");

        return triggers;
    }

    public List<String> getAvailableTriggerActions() {
        List<String> triggers = new ArrayList<>();

        triggers.add("Send line on..");

        return triggers;
    }

    private String consoleToWrite = "";

    public void writeToConsole(String text) {
        consoleToWrite += text;

        class LinuxNodeWriteConsole implements Runnable {
            public void run() {
                if (consoleTextArea != null) {
                    consoleTextArea.appendText(consoleToWrite);

                    List<Trigger> triggers = getProgram().getFlowController().getActiveTriggers(getContainedText(), "New line");
                    for (Trigger trigger : triggers) {
                        NodeRunParams nodeRunParams = new NodeRunParams();
                        nodeRunParams.setOneTimeVariable(consoleToWrite);
                        Program.runHelper(trigger.getParent().getNextNodeToRun(), getProgram().getFlowController().getReferenceID(), trigger.getParent(), false, false, nodeRunParams);
                    }

                    consoleToWrite = "";
                }
            }
        }

        Platform.runLater(new LinuxNodeWriteConsole());
    }

    public void clearConsole() {
        consoleTextArea.clear();
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        String script = "";
        if (nodeRunParams.getOneTimeVariable() instanceof String) {
            script = (String) nodeRunParams.getOneTimeVariable();
        } else if (nodeRunParams.getOneTimeVariable() instanceof BatchTest) {
            script = ((BatchTest) nodeRunParams.getOneTimeVariable()).getText();
        }

        if (script != null) {
            String userHome = System.getProperty("user.home");

            // Save script in .bat file.
            File root = new File(userHome, "/SDE"); // On Windows running on C:\, this is C:\java.
            //File root = new File("C:\\developers\\alex\\svnwork\\focal-v6-demo-test\\SDE\\out\\production\\SDE\\programs"); // On Windows running on C:\, this is C:\java.
            File batchFile = new File(root, "programs/" + getContainedText() + ".bat");
            Boolean mkDirResult = batchFile.getParentFile().mkdirs();

            if (!mkDirResult) {
                log.info("Did not create directory " + batchFile.getAbsolutePath());
            }

            try {
                new FileWriter(batchFile).append(script).close();
            } catch (IOException ex) {
                Error.WRITE_FILE.record().create(ex);
            }

            log.info("Running " + batchFile.getPath());

            String output = SDEUtils.runCMDCommand(batchFile.getPath());

            writeToConsole(output);
        }
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

}
