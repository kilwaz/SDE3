package application.gui.window;

import application.gui.window.setting.RootSettingWindow;
import application.gui.window.setting.SettingsPage;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.net.URL;

public class SettingsPickerWindow extends Stage {
    private static Logger log = Logger.getLogger(SettingsPickerWindow.class);
    private AnchorPane settingContent = new AnchorPane();
    private TreeView<SettingsPage> treeView = null;

    public SettingsPickerWindow() {
        init();
    }

    private void init() {
        HBox hBox = new HBox(5);
        settingContent = new AnchorPane();

        treeView = new TreeView<>();
        treeView.setRoot(new RootSettingWindow());
        treeView.setCellFactory(treeItem -> new TreeCell<SettingsPage>() {
            @Override
            protected void updateItem(SettingsPage item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    setText(item.getName());
                }
            }
        });

        treeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, old_val, selectedSetting) -> {
                    settingContent.getChildren().clear();
                    settingContent.getChildren().add(selectedSetting.getValue().getInterface());
                });

        hBox.getChildren().add(treeView);
        hBox.getChildren().add(settingContent);
        this.setScene(new Scene(hBox, 800, 350));
        this.setTitle("Settings");

        URL url = getClass().getResource("/icon.png");
        this.getIcons().add(new Image(url.toExternalForm()));

        this.show();
    }
}
