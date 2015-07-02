package application.node.implementations;

import application.data.DBConnection;
import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.utils.NodeRunParams;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class DataBaseNode extends DrawableNode {

    private String username = "";
    private String password = "";
    private String connectionString = "";

    private DBConnection dbConnection;

    // This will make a copy of the node passed to it
    public DataBaseNode(DataBaseNode dataBaseNode) {
        this.setId(-1);
        this.setX(dataBaseNode.getX());
        this.setY(dataBaseNode.getY());
        this.setWidth(dataBaseNode.getWidth());
        this.setHeight(dataBaseNode.getHeight());
        this.setColor(dataBaseNode.getColor());
        this.setScale(dataBaseNode.getScale());
        this.setContainedText(dataBaseNode.getContainedText());
        this.setProgramId(dataBaseNode.getProgramId());
        this.setNextNodeToRun(dataBaseNode.getNextNodeToRun());
    }

    public DataBaseNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public DataBaseNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        dbConnection = new DBConnection(connectionString, username, password);
        dbConnection.connect();
    }

    public DBConnection getDbConnection() {
        return dbConnection;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.add(new SavableAttribute("ConnectionString", connectionString.getClass().getName(), connectionString));
        savableAttributes.add(new SavableAttribute("Username", username.getClass().getName(), username));
        savableAttributes.add(new SavableAttribute("Password", password.getClass().getName(), password));
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

        AnchorPane.setBottomAnchor(hbox, 11.0);
        AnchorPane.setLeftAnchor(hbox, 11.0);
        AnchorPane.setRightAnchor(hbox, 11.0);
        AnchorPane.setTopAnchor(hbox, 50.0);

        AnchorPane.setBottomAnchor(rows, 11.0);
        AnchorPane.setLeftAnchor(rows, 11.0);
        AnchorPane.setRightAnchor(rows, 11.0);
        AnchorPane.setTopAnchor(rows, 50.0);

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
        rowLabel.setId("databaseLabel-" + rowName + "-" + this.getId());

        rowField.setText(rowValue);
        rowField.setId("databaseField-" + rowName + "-" + this.getId());
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

                DataBank.saveNode(this);
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
