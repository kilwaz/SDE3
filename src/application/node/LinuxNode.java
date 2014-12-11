package application.node;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.net.SSHCommand;
import application.net.SSHManager;
import application.utils.SDEUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LinuxNode extends DrawableNode {
    private String address = "";
    private String username = "";
    private String password = "";
    private String consoleName = "";

    private SSHManager sshManager = null;

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
        this.setConsoleName(linuxNode.getConsoleName());
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

        rows.getChildren().add(createLinuxNodeRow("Address:", "address", address));
        rows.getChildren().add(createLinuxNodeRow("Username:", "username", username));
        rows.getChildren().add(createLinuxNodeRow("Password:", "password", password));
        rows.getChildren().add(createLinuxNodeRow("Console:", "consoleName", consoleName));

        anchorPane.getChildren().add(rows);

        return tab;
    }

    public HBox createLinuxNodeRow(String labelName, String rowName, String rowValue) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER);
        Label rowLabel = new Label();
        TextField rowField = TextFields.createClearableTextField();

        rowLabel.setPrefWidth(80);
        rowLabel.setText(labelName);
        rowLabel.setId("linuxLabel-" + rowName + "-" + this.getId());

        rowField.setText(rowValue);
        rowField.setId("linuxField-" + rowName + "-" + this.getId());
        rowField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                String[] fieldId = textField.getId().split("-");

                if (fieldId[1].equals("address")) {
                    address = textField.getText();
                } else if (fieldId[1].equals("username")) {
                    username = textField.getText();
                } else if (fieldId[1].equals("password")) {
                    password = textField.getText();
                } else if (fieldId[1].equals("consoleName")) {
                    consoleName = textField.getText();
                    DataBank.currentlyEditProgram.getFlowController().checkConnections();
                }

                //program.getFlowController().checkConnections(); // Renaming a node might make or break connections

                DataBank.saveNode(this);
                Controller.getInstance().updateCanvasControllerNow();
            }
        });

        row.getChildren().add(rowLabel);
        row.getChildren().add(rowField);

        return row;
    }

    public void run(Boolean whileWaiting, HashMap<String, Object> map) {
        String script = (String) map.get("bashScript");
        //System.out.println("Running linux, script is.. " + script);
        if (script != null) {
            String userHome = System.getProperty("user.home");

            // Save source in .java file.
            File root = new File(userHome, "/SDE"); // On Windows running on C:\, this is C:\java.
            //File root = new File("C:\\developers\\alex\\svnwork\\focal-v6-demo-test\\SDE\\out\\production\\SDE\\programs"); // On Windows running on C:\, this is C:\java.
            File sourceFile = new File(root, "programs/bash.script");
            sourceFile.getParentFile().mkdirs();

            //System.out.println("Writing file " + script);

            try {
                new FileWriter(sourceFile).append(script).close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Opening connection..");

            sshManager = SDEUtils.openSSHSession(address, username, password, consoleName, DataBank.currentlyEditProgram.getFlowController().getReferenceID());
            //System.out.println("Transferring file..");
            sshManager.scpTo("/home/" + username + "/bash.sh", sourceFile.getPath());
            //System.out.println("Changing permissions...");
            sshManager.runSSHCommand(new SSHCommand("chmod 777 bash.sh && ./bash.sh", "~$", 100));
            //System.out.println("Done!");
            //sshManager.close();
        } else {
            System.out.println("The bash script was null");
        }
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<SavableAttribute>();

        savableAttributes.add(new SavableAttribute("Address", address.getClass().getName(), address));
        savableAttributes.add(new SavableAttribute("Username", username.getClass().getName(), username));
        savableAttributes.add(new SavableAttribute("Password", password.getClass().getName(), password));
        savableAttributes.add(new SavableAttribute("ConsoleName", consoleName.getClass().getName(), consoleName));
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

    public String getConsoleName() {
        return consoleName;
    }

    public void setConsoleName(String consoleName) {
        this.consoleName = consoleName;
    }

    public Boolean isConnected() {
        if (sshManager != null) {
            return sshManager.isConnected();
        } else {
            return false;
        }
    }
}
