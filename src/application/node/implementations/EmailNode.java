package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.Program;
import application.node.design.DrawableNode;
import application.node.objects.Trigger;
import application.utils.Email;
import application.utils.NodeRunParams;
import application.utils.ReceivedEmail;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class EmailNode extends DrawableNode {
    private String emailUrl = "";
    private String emailPassword = "";
    private String emailUsername = "";

    private Email email = null;

    // This will make a copy of the node passed to it
    public EmailNode(EmailNode emailNode) {
        this.setX(emailNode.getX());
        this.setY(emailNode.getY());
        this.setWidth(emailNode.getWidth());
        this.setHeight(emailNode.getHeight());
        this.setColor(emailNode.getColor());
        this.setScale(emailNode.getScale());
        this.setContainedText(emailNode.getContainedText());
//        this.setProgramUuid(emailNode.getProgramUuid());
        this.setNextNodeToRun(emailNode.getNextNodeToRun());

        this.setEmailUrl(emailNode.getEmailUrl());
        this.setEmailPassword(emailNode.getEmailPassword());
        this.setEmailUsername(emailNode.getEmailUsername());
    }

    public EmailNode() {
        super();
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        // The ordering here is Tab < ScrollPane < AnchorPane
        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        HBox hbox = new HBox(5);
        hbox.getChildren().add(createEmailNodeRow("URL:", "emailUrl", emailUrl));
        hbox.getChildren().add(createEmailNodeRow("Username:", "emailUsername", emailUsername));
        hbox.getChildren().add(createEmailNodeRow("Password:", "emailPassword", emailPassword));

        rows.getChildren().add(hbox);

        anchorPane.getChildren().add(rows);

        // Go back to the beginning and run the code to show the tab, it should now exist
        return tab;
    }

    public List<String> getAvailableTriggers() {
        List<String> triggers = new ArrayList<>();

        triggers.add("Receive New Email");

        return triggers;
    }

    public List<String> getAvailableTriggerActions() {
        List<String> triggers = new ArrayList<>();

        triggers.add("Pass Email on...");

        return triggers;
    }

    public void newEmailTrigger(ReceivedEmail receivedEmail) {
        class EmailNodeNewEmail implements Runnable {
            public void run() {
                List<Trigger> triggers = getProgram().getFlowController().getActiveTriggers(getContainedText(), "Receive New Email");
                for (Trigger trigger : triggers) {
                    NodeRunParams nodeRunParams = new NodeRunParams();
                    nodeRunParams.setOneTimeVariable(receivedEmail);
                    Program.runHelper(trigger.getParent().getNextNodeToRun(), getProgram().getFlowController().getReferenceID(), trigger.getParent(), false, true, null, nodeRunParams);
                }
            }
        }

        Platform.runLater(new EmailNodeNewEmail());
    }

    public HBox createEmailNodeRow(String labelName, String rowName, String rowValue) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER);
        Label rowLabel = new Label();
        TextField rowField = new TextField();

        rowLabel.setText(labelName);
        rowLabel.setId("emailLabel-" + rowName + "-" + this.getUuidString());

        rowField.setText(rowValue);
        rowField.setId("emailField-" + rowName + "-" + this.getUuidString());
        rowField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                String[] fieldId = textField.getId().split("-");

                switch (fieldId[1]) {
                    case "emailUrl":
                        emailUrl = textField.getText();
                        break;
                    case "emailUsername":
                        emailUsername = textField.getText();
                        break;
                    case "emailPassword":
                        emailPassword = textField.getText();
                        break;
                }

                save();
            }
        });

        row.getChildren().add(rowLabel);
        row.getChildren().add(rowField);

        return row;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        // EmailUrl
        SavableAttribute emailURLAttribute = SavableAttribute.create(SavableAttribute.class);
        emailURLAttribute.init("EmailUrl", emailUrl.getClass().getName(), emailUrl, this);
        savableAttributes.add(emailURLAttribute);

        // EmailPassword
        SavableAttribute emailPasswordAttribute = SavableAttribute.create(SavableAttribute.class);
        emailPasswordAttribute.init("EmailUsername", emailUsername.getClass().getName(), emailUsername, this);
        savableAttributes.add(emailPasswordAttribute);

        // EmailUsername
        SavableAttribute emailUsernameAttribute = SavableAttribute.create(SavableAttribute.class);
        emailUsernameAttribute.init("EmailPassword", emailPassword.getClass().getName(), emailPassword, this);
        savableAttributes.add(emailUsernameAttribute);

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        email = new Email(emailUrl, emailUsername, emailPassword, this);
    }

    public String getEmailUrl() {
        return emailUrl;
    }

    public void setEmailUrl(String emailUrl) {
        this.emailUrl = emailUrl;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public String getEmailUsername() {
        return emailUsername;
    }

    public void setEmailUsername(String emailUsername) {
        this.emailUsername = emailUsername;
    }
}
