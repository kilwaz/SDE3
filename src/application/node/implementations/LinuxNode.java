package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.Program;
import application.net.SSHCommand;
import application.net.SSHManager;
import application.node.design.DrawableNode;
import application.node.objects.Trigger;
import application.utils.NodeRunParams;
import application.utils.SDEUtils;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LinuxNode extends DrawableNode {
    private String address = "";
    private String username = "";
    private String password = "";

    private SSHManager sshManager = null;
    private TextArea consoleTextArea = new TextArea();

    // Not to be saved in the database
    private String consoleToWrite = "";

    // This will make a copy of the node passed to it
    public LinuxNode(LinuxNode linuxNode) {
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

        this.setAddress(linuxNode.getAddress());
        this.setUsername(linuxNode.getUsername());
        this.setPassword(linuxNode.getPassword());
    }


    public LinuxNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public LinuxNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
    }

    public LinuxNode(Double x, Double y, Double width, Double height, Color color, String containedText, Integer programId, Integer id) {
        super(x, y, width, height, color, containedText, programId, id);
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        HBox hbox = new HBox(5);
        hbox.getChildren().add(createLinuxNodeRow("Username:", "username", username));
        hbox.getChildren().add(createLinuxNodeRow("Password:", "password", password));
        hbox.getChildren().add(createLinuxNodeRow("Address:", "address", address));

        AnchorPane.setBottomAnchor(hbox, 11.0);
        AnchorPane.setLeftAnchor(hbox, 11.0);
        AnchorPane.setRightAnchor(hbox, 11.0);
        AnchorPane.setTopAnchor(hbox, 50.0);

        AnchorPane.setBottomAnchor(rows, 11.0);
        AnchorPane.setLeftAnchor(rows, 11.0);
        AnchorPane.setRightAnchor(rows, 11.0);
        AnchorPane.setTopAnchor(rows, 50.0);

        rows.getChildren().add(hbox);
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

    public HBox createLinuxNodeRow(String labelName, String rowName, String rowValue) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER);
        Label rowLabel = new Label();
//        TextField rowField = TextFields.createClearableTextField();
        TextField rowField = new TextField();

        rowLabel.setText(labelName);
        rowLabel.setId("linuxLabel-" + rowName + "-" + this.getId());

        rowField.setText(rowValue);
        rowField.setId("linuxField-" + rowName + "-" + this.getId());
        rowField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                String[] fieldId = textField.getId().split("-");

                switch (fieldId[1]) {
                    case "address":
                        address = textField.getText();
                        break;
                    case "username":
                        username = textField.getText();
                        break;
                    case "password":
                        password = textField.getText();
                        break;
                }

                DataBank.saveNode(this);
            }
        });

        row.getChildren().add(rowLabel);
        row.getChildren().add(rowField);

        return row;
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
        String script = (String) nodeRunParams.getOneTimeVariable();
        //System.out.println("Running linux, script is.. " + script);
        if (script != null) {
            String userHome = System.getProperty("user.home");

            // Save source in .java file.
            File root = new File(userHome, "/SDE"); // On Windows running on C:\, this is C:\java.
            //File root = new File("C:\\developers\\alex\\svnwork\\focal-v6-demo-test\\SDE\\out\\production\\SDE\\programs"); // On Windows running on C:\, this is C:\java.
            File sourceFile = new File(root, "programs/bash.script");
            Boolean mkDirResult = sourceFile.getParentFile().mkdirs();

            if (!mkDirResult) {
                System.out.println("Issue creating directory " + sourceFile.getAbsolutePath());
            }

            //System.out.println("Writing file " + script);

            try {
                new FileWriter(sourceFile).append(script).close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Opening connection..");

            // Here we open a new ssh connection with the details given
            sshManager = SDEUtils.openSSHSession(address, username, password, this);
            // Transfer the bash file
            sshManager.scpTo("/home/" + username + "/bash.sh", sourceFile.getPath());
            // Change the permissions of the bash file so that we can execute it and finally execute the bash file afterwards
            sshManager.runSSHCommand(new SSHCommand("chmod 777 bash.sh && ./bash.sh", "~$", 100));
        } else {
            System.out.println("The bash script was null");
        }
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.add(new SavableAttribute("Address", address.getClass().getName(), address));
        savableAttributes.add(new SavableAttribute("Username", username.getClass().getName(), username));
        savableAttributes.add(new SavableAttribute("Password", password.getClass().getName(), password));
        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean isConnected() {
        if (sshManager != null) {
            return sshManager.isConnected();
        } else {
            return false;
        }
    }

    public void closeSSHManager() {
        sshManager.close();
        Controller.getInstance().updateCanvasControllerLater();
    }
}
