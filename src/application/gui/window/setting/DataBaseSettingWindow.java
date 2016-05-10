package application.gui.window.setting;

import application.data.DBConnectionManager;
import application.data.DataBank;
import application.gui.Controller;
import application.gui.dialog.ConfirmDialog;
import application.utils.AppParams;
import application.utils.AppProperties;
import application.utils.managers.DatabaseObjectManager;
import application.utils.managers.SessionManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;

public class DataBaseSettingWindow extends SettingsPage {
    private static Logger log = Logger.getLogger(DataBaseSettingWindow.class);

    private ToggleGroup databaseGroup;
    private RadioButton localDatabase;
    private RadioButton remoteDatabase;

    private TextField localDBNameTextField;
    private TextField dbConnectionStringTextField;
    private TextField dbUsernameTextField;
    private TextField dbPasswordTextField;

    private Button switchDatabaseButton;
    private Button rebuildDatabaseButton;
    private Button createNewDatabaseButton;

    public DataBaseSettingWindow() {
        super();
    }

    public void setupChildren() {

    }

    public AnchorPane getInterface() {
        VBox rows = new VBox(5);
        rows.setPadding(new Insets(10, 10, 10, 10));

        // SETUP GROUP RADIO BUTTONS
        databaseGroup = new ToggleGroup();
        databaseGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                RadioButton selectedButton = (RadioButton) new_toggle;
                if ("localRadio".equals(selectedButton.getId())) {
                    AppParams.setLocalDatabase(true);
                } else if ("remoteRadio".equals(selectedButton.getId())) {
                    AppParams.setLocalDatabase(false);
                }
                updateFieldAccess();
            }
        });

        localDatabase = new RadioButton();
        localDatabase.setText("Use Local Database");
        localDatabase.setSelected(AppParams.isLocalDatabase());
        localDatabase.setToggleGroup(databaseGroup);
        localDatabase.setId("localRadio");

        remoteDatabase = new RadioButton();
        remoteDatabase.setText("Use Remote Database");
        remoteDatabase.setSelected(!AppParams.isLocalDatabase());
        remoteDatabase.setToggleGroup(databaseGroup);
        remoteDatabase.setId("remoteRadio");

        // LOCAL DB NAME
        HBox localDBNameRow = new HBox(5);
        localDBNameRow.setPadding(new Insets(0, 0, 0, 40));

        Label localDBNameLabel = new Label();
        localDBNameLabel.setText("Database Name: ");
        localDBNameLabel.setPrefWidth(200.0);

        localDBNameTextField = new TextField();
        localDBNameTextField.setPrefWidth(200.0);
        localDBNameTextField.setText(AppParams.getLocalDatabaseName());
        localDBNameTextField.setOnKeyReleased(event -> AppParams.setLocalDatabaseName(((TextField) event.getSource()).getText()));

        localDBNameRow.getChildren().add(localDBNameLabel);
        localDBNameRow.getChildren().add(localDBNameTextField);

        // REMOTE DB CONNECTION STRING
        HBox dbConnectionStringRow = new HBox(5);
        dbConnectionStringRow.setPadding(new Insets(0, 0, 0, 40));

        Label dbConnectionStringLabel = new Label();
        dbConnectionStringLabel.setText("Database connection string: ");
        dbConnectionStringLabel.setPrefWidth(200.0);

        dbConnectionStringTextField = new TextField();
        dbConnectionStringTextField.setPrefWidth(200.0);
        dbConnectionStringTextField.setText(AppParams.getRemoteDatabaseConnection());
        dbConnectionStringTextField.setOnKeyReleased(event -> AppParams.setRemoteDatabaseConnection(((TextField) event.getSource()).getText()));

        dbConnectionStringRow.getChildren().add(dbConnectionStringLabel);
        dbConnectionStringRow.getChildren().add(dbConnectionStringTextField);

        // DB USERNAME
        HBox dbUsernameRow = new HBox(5);
        dbUsernameRow.setPadding(new Insets(0, 0, 0, 40));

        Label dbUsernameLabel = new Label();
        dbUsernameLabel.setText("Username: ");
        dbUsernameLabel.setPrefWidth(200.0);

        dbUsernameTextField = new TextField();
        dbUsernameTextField.setPrefWidth(200.0);
        dbUsernameTextField.setText(AppParams.getRemoteDatabaseUsername());
        dbUsernameTextField.setOnKeyReleased(event -> AppParams.setRemoteDatabaseUsername(((TextField) event.getSource()).getText()));

        dbUsernameRow.getChildren().add(dbUsernameLabel);
        dbUsernameRow.getChildren().add(dbUsernameTextField);

        // DB PASSWORD
        HBox dbPasswordRow = new HBox(5);
        dbPasswordRow.setPadding(new Insets(0, 0, 0, 40));

        Label dbPasswordLabel = new Label();
        dbPasswordLabel.setText("Password: ");
        dbPasswordLabel.setPrefWidth(200.0);

        dbPasswordTextField = new TextField();
        dbPasswordTextField.setPrefWidth(200.0);
        dbPasswordTextField.setText(AppParams.getRemoteDatabasePassword());
        dbPasswordTextField.setOnKeyReleased(event -> AppParams.setRemoteDatabasePassword(((TextField) event.getSource()).getText()));

        dbPasswordRow.getChildren().add(dbPasswordLabel);
        dbPasswordRow.getChildren().add(dbPasswordTextField);

        switchDatabaseButton = new Button();
        switchDatabaseButton.setText("Switch to Database");
        switchDatabaseButton.setOnAction(event -> {
            AppProperties.saveToXML();
            DatabaseObjectManager.getInstance().clearAllObjects();
            Controller.getInstance().closeAllNodeTabs();
            DBConnectionManager.getInstance().createApplicationConnection();
            SessionManager.getInstance().clearAllSessions();
            DataBank.createCurrentSession();
            DataBank.getNodeColours().reloadNodeColours();
            Controller.getInstance().reloadPrograms();
            Controller.getInstance().updateCanvasControllerLater();
        });

        rebuildDatabaseButton = new Button();
        rebuildDatabaseButton.setText("Rebuild this Database");
        rebuildDatabaseButton.setOnAction(event -> {
            application.gui.dialog.Dialog confirmDialog = new ConfirmDialog()
                    .content("Are you sure you want to rebuild " + DBConnectionManager.getInstance().getApplicationConnection().getConnectionString() + "?  This will delete all existing data.")
                    .title("Rebuild Database")
                    .onYesAction(() -> {
                        DBConnectionManager.getInstance().getApplicationConnection().rebuildDatabase();
                        AppProperties.saveToXML();
                        DatabaseObjectManager.getInstance().clearAllObjects();
                        Controller.getInstance().closeAllNodeTabs();
                        DBConnectionManager.getInstance().createApplicationConnection();
                        SessionManager.getInstance().clearAllSessions();
                        DataBank.createCurrentSession();
                        DataBank.getNodeColours().reloadNodeColours();
                        Controller.getInstance().reloadPrograms();
                        Controller.getInstance().updateCanvasControllerLater();
                    });
            confirmDialog.show();
        });

        createNewDatabaseButton = new Button();
        createNewDatabaseButton.setText("Create new Database");
        createNewDatabaseButton.setOnAction(event -> {
            application.gui.dialog.Dialog confirmDialog = new ConfirmDialog()
                    .content("Are you sure you want to create " + DBConnectionManager.getInstance().getApplicationConnection().getConnectionString() + "?  This will delete all existing data.")
                    .title("Create Database")
                    .onYesAction(() -> {
                        AppProperties.saveToXML();
                        DatabaseObjectManager.getInstance().clearAllObjects();
                        Controller.getInstance().closeAllNodeTabs();
                        DBConnectionManager.getInstance().createApplicationConnection();
                        DBConnectionManager.getInstance().getApplicationConnection().rebuildDatabase();
                        SessionManager.getInstance().clearAllSessions();
                        DataBank.createCurrentSession();
                        DataBank.getNodeColours().reloadNodeColours();
                        Controller.getInstance().reloadPrograms();
                        Controller.getInstance().updateCanvasControllerLater();
                    });
            confirmDialog.show();
        });

        rows.getChildren().add(localDatabase);
        rows.getChildren().add(localDBNameRow);
        rows.getChildren().add(remoteDatabase);
        rows.getChildren().add(dbConnectionStringRow);
        rows.getChildren().add(dbUsernameRow);
        rows.getChildren().add(dbPasswordRow);

        HBox buttonRow = new HBox(5);
        buttonRow.getChildren().add(createNewDatabaseButton);
        buttonRow.getChildren().add(switchDatabaseButton);
        buttonRow.getChildren().add(rebuildDatabaseButton);

        rows.getChildren().add(buttonRow);
        StackPane root = new StackPane();

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(rows);
        root.getChildren().add(anchorPane);

        updateFieldAccess();

        return anchorPane;
    }

    private void updateFieldAccess() {
        if (AppParams.isLocalDatabase()) {
            if (localDBNameTextField != null) {
                localDBNameTextField.setDisable(false);
                dbConnectionStringTextField.setDisable(true);
                dbUsernameTextField.setDisable(true);
                dbPasswordTextField.setDisable(true);
            }

        } else {
            if (localDBNameTextField != null) {
                localDBNameTextField.setDisable(true);
                dbConnectionStringTextField.setDisable(false);
                dbUsernameTextField.setDisable(false);
                dbPasswordTextField.setDisable(false);
            }
        }
    }

    public String getName() {
        return "Database";
    }
}
