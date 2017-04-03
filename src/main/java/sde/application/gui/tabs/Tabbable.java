package sde.application.gui.tabs;

import sde.application.utils.managers.TabManager;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.apache.log4j.Logger;

public class Tabbable {
    private Tab controlTab;
    private String tabTitle = "Untitled";
    private Label controlLabel;
    private TabContainer parent;

    private static Logger log = Logger.getLogger(Tabbable.class);

    public Tabbable() {
        controlLabel = new Label(tabTitle);

        controlLabel.setOnDragDetected(event -> {
            Dragboard dragboard = controlLabel.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString("SDETab");
            dragboard.setContent(clipboardContent);
            TabManager.getInstance().setCurrentlyDragging(new TabDragAction(this, parent));
            event.consume();
        });

        controlTab = new Tab();
        controlTab.setGraphic(controlLabel);
    }

    public Tab getControlTab() {
        return controlTab;
    }

    public String getTabTitle() {
        return tabTitle;
    }

    public void setTabTitle(String tabTitle) {
        controlLabel.setText(tabTitle);
        this.tabTitle = tabTitle;
    }

    public TabContainer getParent() {
        return parent;
    }

    public void setParent(TabContainer parent) {
        this.parent = parent;
    }
}
