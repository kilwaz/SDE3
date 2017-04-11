package sde.application.gui.window.setting.browsers;

import javafx.geometry.Insets;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import sde.application.gui.window.setting.SettingsPage;

public class ChromeSettings extends SettingsPage {

    public ChromeSettings() {
        super();
    }

    public void setupChildren() {
    }

    public AnchorPane getInterface() {
        VBox rows = new VBox(5);
        rows.setPadding(new Insets(10, 10, 10, 10));

        StackPane root = new StackPane();

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(rows);
        root.getChildren().add(anchorPane);

        return anchorPane;
    }

    public String getName() {
        return "Chrome";
    }
}
