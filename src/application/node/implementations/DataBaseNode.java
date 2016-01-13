package application.node.implementations;

import application.data.DBConnection;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.UI;
import application.node.design.DrawableNode;
import application.utils.NodeRunParams;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class DataBaseNode extends DrawableNode {

    private String username = "";
    private String password = "";
    private String connectionString = "";

    private List<DBConnection> dbConnections = new ArrayList<>();

    private static Logger log = Logger.getLogger(DataBaseNode.class);

    // This will make a copy of the node passed to it
    public DataBaseNode(DataBaseNode dataBaseNode) {
        this.setX(dataBaseNode.getX());
        this.setY(dataBaseNode.getY());
        this.setWidth(dataBaseNode.getWidth());
        this.setHeight(dataBaseNode.getHeight());
        this.setColor(dataBaseNode.getColor());
        this.setScale(dataBaseNode.getScale());
        this.setContainedText(dataBaseNode.getContainedText());
        this.setNextNodeToRun(dataBaseNode.getNextNodeToRun());
    }

    public DataBaseNode() {
        super();
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        if (getDbConnection(connectionString) == null) {
            connectToDatabase();
        } else if (!getDbConnection(connectionString).isConnected()) {
            log.info("DBConnection " + connectionString + " was closed and so was removed from available list");
            removeDBConnection(connectionString);
            connectToDatabase();
        } else {
            log.info("DBConnection " + connectionString + " already established");
        }
    }

    private void connectToDatabase() {
        DBConnection dbConnection = new DBConnection(connectionString, username, password);
        dbConnection.connect();
        dbConnections.add(dbConnection);
    }

    public void removeDBConnection(String connectionString) {
        DBConnection connectionToRemove = null;
        for (DBConnection dbConnection : dbConnections) {
            if (connectionString != null && connectionString.equals(dbConnection.getConnectionString())) {
                connectionToRemove = dbConnection;
            }
        }
        if (connectionToRemove != null) {
            dbConnections.remove(connectionToRemove);
        }
    }

    public DBConnection getDbConnection(String connectionString) {
        for (DBConnection dbConnection : dbConnections) {
            if (connectionString != null && connectionString.equals(dbConnection.getConnectionString())) {
                return dbConnection;
            }
        }
        return null;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        // ConnectionString
        SavableAttribute connectionStringAttribute = SavableAttribute.create(SavableAttribute.class);
        connectionStringAttribute.init("ConnectionString", connectionString.getClass().getName(), connectionString, this);
        savableAttributes.add(connectionStringAttribute);

        // Username
        SavableAttribute usernameAttribute = SavableAttribute.create(SavableAttribute.class);
        usernameAttribute.init("Username", username.getClass().getName(), username, this);
        savableAttributes.add(usernameAttribute);

        // Password
        SavableAttribute passwordAttribute = SavableAttribute.create(SavableAttribute.class);
        passwordAttribute.init("Password", password.getClass().getName(), password, this);
        savableAttributes.add(passwordAttribute);

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        HBox hbox = new HBox(5);
        hbox.getChildren().add(createDatabaseNodeRow("Username:", "username", username));
        hbox.getChildren().add(createDatabaseNodeRow("Password:", "password", password));
        hbox.getChildren().add(createDatabaseNodeRow("Url:", "connectionString", connectionString));

        UI.setAnchorMargins(hbox, 50.0, 11.0, 11.0, 11.0);
        UI.setAnchorMargins(rows, 50.0, 11.0, 11.0, 11.0);

        rows.getChildren().add(hbox);

        anchorPane.getChildren().add(rows);

        return tab;
    }

    public HBox createDatabaseNodeRow(String labelName, String rowName, String rowValue) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER);
        Label rowLabel = new Label();
        TextField rowField = new TextField();

        rowLabel.setText(labelName);
        rowLabel.setId("databaseLabel-" + rowName + "-" + this.getUuidStringWithoutHyphen());

        rowField.setText(rowValue);
        rowField.setId("databaseField-" + rowName + "-" + this.getUuidStringWithoutHyphen());
        rowField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                String[] fieldId = textField.getId().split("-");

                switch (fieldId[1]) {
                    case "connectionString":
                        connectionString = textField.getText();
                        break;
                    case "username":
                        username = textField.getText();
                        break;
                    case "password":
                        password = textField.getText();
                        break;
                }

                save();
            }
        });

        row.getChildren().add(rowLabel);
        row.getChildren().add(rowField);

        return row;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
