package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.Program;
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
import javafx.scene.paint.Color;
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
        this.setId(-1);
        this.setX(linuxNode.getX());
        this.setY(linuxNode.getY());
        this.setWidth(linuxNode.getWidth());
        this.setHeight(linuxNode.getHeight());
        this.setColor(linuxNode.getColor());
        this.setScale(linuxNode.getScale());
        this.setContainedText(linuxNode.getContainedText());
        this.setProgramId(linuxNode.getProgramId());
        this.setNextNodeToRun(linuxNode.getNextNodeToRun());
    }


    public WindowsNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public WindowsNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
    }

    public WindowsNode(Double x, Double y, Double width, Double height, Color color, String containedText, Integer programId, Integer id) {
        super(x, y, width, height, color, containedText, programId, id);
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        AnchorPane.setBottomAnchor(rows, 11.0);
        AnchorPane.setLeftAnchor(rows, 11.0);
        AnchorPane.setRightAnchor(rows, 11.0);
        AnchorPane.setTopAnchor(rows, 50.0);

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

                    List<Trigger> triggers = DataBank.currentlyEditProgram.getFlowController().getActiveTriggers(getContainedText(), "New line");
                    for (Trigger trigger : triggers) {
                        NodeRunParams nodeRunParams = new NodeRunParams();
                        nodeRunParams.setOneTimeVariable(consoleToWrite);
                        Program.runHelper(trigger.getParent().getNextNodeToRun(), DataBank.currentlyEditProgram.getFlowController().getReferenceID(), trigger.getParent(), false, false, nodeRunParams);
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
                log.error(ex);
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
