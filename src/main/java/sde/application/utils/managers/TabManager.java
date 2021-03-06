package sde.application.utils.managers;

import sde.application.gui.tabs.TabContainer;
import sde.application.gui.tabs.TabDragAction;
import sde.application.gui.tabs.Tabbable;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class TabManager {
    private static TabManager instance;
    private TabDragAction currentlyDragging = null;

    public TabManager() {
        //createTestStage("Window 1");
        //createTestStage("Window 2");

        instance = this;
    }

    public static TabManager getInstance() {
        return instance;
    }

    public void createTestStage(String title) {
        Stage stage = new Stage();
        stage.setTitle(title);

        StackPane root = new StackPane();

        TabContainer tabContainer = new TabContainer();
        Tabbable tabbable = new Tabbable();
        tabbable.setTabTitle("Tab in " + title);
        tabContainer.addTab(tabbable);
        root.getChildren().add(tabContainer);

        stage.setScene(new Scene(root, 900, 800));
        stage.show();
    }

    public TabDragAction getCurrentlyDragging() {
        return currentlyDragging;
    }

    public void setCurrentlyDragging(TabDragAction currentlyDragging) {
        this.currentlyDragging = currentlyDragging;
    }
}
