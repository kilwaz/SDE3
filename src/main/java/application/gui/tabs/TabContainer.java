package application.gui.tabs;

import application.utils.managers.TabManager;
import javafx.scene.control.TabPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class TabContainer extends TabPane {
    private List<Tabbable> tabList = new ArrayList<>();

    private static Logger log = Logger.getLogger(TabContainer.class);

    public TabContainer() {
        this.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString() && "SDETab".equals(dragboard.getString())) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });

        this.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString() && "SDETab".equals(dragboard.getString())) {
                TabDragAction tabDragAction = TabManager.getInstance().getCurrentlyDragging();

                if (tabDragAction != null) {
                    tabDragAction.getSourceContainer().removeTab(tabDragAction.getTabBeingDragged());
                    this.addTab(tabDragAction.getTabBeingDragged());
                }

                TabManager.getInstance().setCurrentlyDragging(null);
                event.setDropCompleted(true);
                event.consume();
            }
        });
    }

    public void removeTab(Tabbable tabbable) {
        this.getTabs().remove(tabbable.getControlTab());
        tabList.remove(tabbable);
    }

    public void addTab(Tabbable tabbable) {
        this.getTabs().add(tabbable.getControlTab());
        this.getSelectionModel().select(tabbable.getControlTab());
        tabbable.setParent(this);
        tabList.add(tabbable);
    }
}
