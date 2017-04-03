package application.gui.window.setting;

import org.apache.log4j.Logger;

public class RootSettingWindow extends SettingsPage {
    private static Logger log = Logger.getLogger(RootSettingWindow.class);

    public RootSettingWindow() {
        super();
    }

    public void setupChildren() {
        getChildren().add(new DataBaseSettingWindow());
        getChildren().add(new DocumentCreationSettingWindow());
        getChildren().add(new LogSettingsWindow());
        getChildren().add(new SeleniumHubControl());
    }

    public String getName() {
        return "Settings";
    }
}

