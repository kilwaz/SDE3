package application.gui.tabs;

public class TabDragAction {
    private Tabbable tabBeingDragged;
    private TabContainer sourceContainer;

    public TabDragAction(Tabbable tabBeingDragged, TabContainer sourceContainer) {
        this.sourceContainer = sourceContainer;
        this.tabBeingDragged = tabBeingDragged;
    }

    public Tabbable getTabBeingDragged() {
        return tabBeingDragged;
    }

    public void setTabBeingDragged(Tabbable tabBeingDragged) {
        this.tabBeingDragged = tabBeingDragged;
    }

    public TabContainer getSourceContainer() {
        return sourceContainer;
    }

    public void setSourceContainer(TabContainer sourceContainer) {
        this.sourceContainer = sourceContainer;
    }
}
