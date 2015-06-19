package application.gui.window;

import application.utils.AppParams;
import application.utils.AppProperties;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsWindow extends Stage {
    public SettingsWindow() {
        init();
    }

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
            dbConnectionStringTextField.setText(AppParams.MYSQL_CONNECTION);
            dbConnectionStringTextField.setOnKeyReleased(event -> AppParams.MYSQL_CONNECTION = ((TextField) event.getSource()).getText());

            dbConnectionStringRow.getChildren().add(dbConnectionStringLabel);
            dbConnectionStringRow.getChildren().add(dbConnectionStringTextField);

            // DB USERNAME
            HBox dbUsernameRow = new HBox(5);

            Label dbUsernameLabel = new Label();
            dbUsernameLabel.setText("Username: ");
            dbUsernameLabel.setPrefWidth(200.0);

            TextField dbUsernameTextField = new TextField();
            dbUsernameTextField.setPrefWidth(200.0);
            dbUsernameTextField.setText(AppParams.MYSQL_USERNAME);
            dbUsernameTextField.setOnKeyReleased(event -> AppParams.MYSQL_USERNAME = ((TextField) event.getSource()).getText());

            dbUsernameRow.getChildren().add(dbUsernameLabel);
            dbUsernameRow.getChildren().add(dbUsernameTextField);

            // DB PASSWORD
            HBox dbPasswordRow = new HBox(5);

            Label dbPasswordLabel = new Label();
            dbPasswordLabel.setText("Password: ");
            dbPasswordLabel.setPrefWidth(200.0);

            TextField dbPasswordTextField = new TextField();
            dbPasswordTextField.setPrefWidth(200.0);
            dbPasswordTextField.setText(AppParams.MYSQL_PASSWORD);
            dbPasswordTextField.setOnKeyReleased(event -> AppParams.MYSQL_PASSWORD = ((TextField) event.getSource()).getText());

            dbPasswordRow.getChildren().add(dbPasswordLabel);
            dbPasswordRow.getChildren().add(dbPasswordTextField);

            Button saveButton = new Button();
            saveButton.setText("Save");
            saveButton.setOnAction(event -> {
                AppProperties.saveToXML();
                this.close();
            });

            rows.getChildren().add(dbConnectionStringRow);
            rows.getChildren().add(dbUsernameRow);
            rows.getChildren().add(dbPasswordRow);
            rows.getChildren().add(saveButton);

            StackPane root = new StackPane();
            AnchorPane anchorPane = new AnchorPane();

            anchorPane.getChildren().add(rows);
            root.getChildren().add(anchorPane);

            this.setScene(new Scene(root, 450, 150));
            this.setTitle("Settings");
            this.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
