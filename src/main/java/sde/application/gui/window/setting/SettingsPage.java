package sde.application.gui.window.setting;

import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;

public class SettingsPage extends TreeItem<SettingsPage> {
    private static Logger log = Logger.getLogger(SettingsPage.class);

    public SettingsPage() {
        setValue(this);
        setupChildren();
        setExpanded(true);
    }

    public void setupChildren() {

    }

    @Override
    public boolean isLeaf() {
        return getChildren().size() == 0;
    }

    public AnchorPane getInterface() {
        return new AnchorPane();
    }

    public String getName() {
        return getClass().getSimpleName();
    }
}
