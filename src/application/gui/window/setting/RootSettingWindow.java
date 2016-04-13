package application.gui.window.setting;

import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;

public class RootSettingWindow extends SettingsPage {
    private static Logger log = Logger.getLogger(RootSettingWindow.class);

    public RootSettingWindow() {
        super();
    }

    public void setupChildren() {
        getChildren().add(new DataBaseSettingWindow());
        getChildren().add(new DocumentCreationSettingWindow());
    }

    public String getName() {
        return "Settings";
    }
}

