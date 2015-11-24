package application.gui.window;

import application.data.DBConnection;
import application.data.DBConnectionManager;
import application.error.Error;
import application.utils.AppParams;
import application.utils.AppProperties;
import application.utils.SDEUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

public class SettingsWindow extends Stage {
    public SettingsWindow() {
        init();
    }

    private static Logger log = Logger.getLogger(SettingsWindow.class);

    private void init() {
        try {
            VBox rows = new VBox(5);
            rows.setLayoutY(10);
            rows.setLayoutX(10);

            // DB CONNECTION STRING
            HBox dbConnectionStringRow = new HBox(5);

            Label dbConnectionStringLabel = new Label();
            dbConnectionStringLabel.setText("DB Connection String: ");
            dbConnectionStringLabel.setPrefWidth(200.0);

            TextField dbConnectionStringTextField = new TextField();
            dbConnectionStringTextField.setPrefWidth(200.0);
            dbConnectionStringTextField.setText(AppParams.getDatabaseConnection());
            dbConnectionStringTextField.setOnKeyReleased(event -> AppParams.setDatabaseConnection(((TextField) event.getSource()).getText()));

            dbConnectionStringRow.getChildren().add(dbConnectionStringLabel);
            dbConnectionStringRow.getChildren().add(dbConnectionStringTextField);

            // DB USERNAME
            HBox dbUsernameRow = new HBox(5);

            Label dbUsernameLabel = new Label();
            dbUsernameLabel.setText("Username: ");
            dbUsernameLabel.setPrefWidth(200.0);

            TextField dbUsernameTextField = new TextField();
            dbUsernameTextField.setPrefWidth(200.0);
            dbUsernameTextField.setText(AppParams.getDatabaseUsername());
            dbUsernameTextField.setOnKeyReleased(event -> AppParams.setDatabasePassword(((TextField) event.getSource()).getText()));

            dbUsernameRow.getChildren().add(dbUsernameLabel);
            dbUsernameRow.getChildren().add(dbUsernameTextField);

            // DB PASSWORD
            HBox dbPasswordRow = new HBox(5);

            Label dbPasswordLabel = new Label();
            dbPasswordLabel.setText("Password: ");
            dbPasswordLabel.setPrefWidth(200.0);

            TextField dbPasswordTextField = new TextField();
            dbPasswordTextField.setPrefWidth(200.0);
            dbPasswordTextField.setText(AppParams.getDatabasePassword());
            dbPasswordTextField.setOnKeyReleased(event -> AppParams.setDatabasePassword(((TextField) event.getSource()).getText()));

            dbPasswordRow.getChildren().add(dbPasswordLabel);
            dbPasswordRow.getChildren().add(dbPasswordTextField);

            Button saveButton = new Button();
            saveButton.setText("Save");
            saveButton.setOnAction(event -> {
                AppProperties.saveToXML();
                this.close();
            });

            Button buildNewDatabase = new Button();
            buildNewDatabase.setText("Build new database");
            buildNewDatabase.setOnAction(event -> {
                try {
                    DBConnection sqliteConnection = DBConnectionManager.getInstance().getApplicationConnection();

                    sqliteConnection.getConnection().setAutoCommit(false);

                    String resourcesPath = SDEUtils.getResourcePath();
                    String bashEditorPath = resourcesPath + "/data/blankdb.sql";

                    String content = "";
                    try {
                        byte[] encoded = Files.readAllBytes(Paths.get(bashEditorPath));
                        content = new String(encoded, "UTF8");

                        String[] sqlQuery = content.split(";");

                        for (String query : sqlQuery) {
                            sqliteConnection.getPreparedStatement(query).execute();
                        }
                    } catch (IOException ex) {
                        application.error.Error.ACE_TEXT_PASTE.record().create(ex);
                    }

                    sqliteConnection.getConnection().commit();
                    sqliteConnection.getConnection().setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            rows.getChildren().add(dbConnectionStringRow);
            rows.getChildren().add(dbUsernameRow);
            rows.getChildren().add(dbPasswordRow);
            rows.getChildren().add(buildNewDatabase);
            rows.getChildren().add(saveButton);

            StackPane root = new StackPane();
            AnchorPane anchorPane = new AnchorPane();

            anchorPane.getChildren().add(rows);
            root.getChildren().add(anchorPane);

            this.setScene(new Scene(root, 450, 150));
            this.setTitle("Settings");

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            Error.CREATE_SETTINGS_WINDOW.record().create(ex);
        }
    }
}
