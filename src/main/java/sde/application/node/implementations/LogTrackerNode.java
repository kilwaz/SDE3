package sde.application.node.implementations;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import sde.application.data.SavableAttribute;
import sde.application.gui.Controller;
import sde.application.gui.UI;
import sde.application.gui.columns.browserlog.LogDateColumn;
import sde.application.gui.columns.browserlog.LogLevelColumn;
import sde.application.gui.columns.browserlog.LogMessageColumn;
import sde.application.node.design.DrawableNode;
import sde.application.node.objects.BrowserLog;
import sde.application.node.objects.BrowserLogListener;
import sde.application.utils.NodeRunParams;

import java.util.ArrayList;
import java.util.List;

public class LogTrackerNode extends DrawableNode implements BrowserLogListener {
    private ObservableList<BrowserLog> browserLogList = FXCollections.observableArrayList();

    // This will make a copy of the node passed to it
    public LogTrackerNode(LogTrackerNode logTrackerNode) {
        this.setX(logTrackerNode.getX());
        this.setY(logTrackerNode.getY());
        this.setWidth(logTrackerNode.getWidth());
        this.setHeight(logTrackerNode.getHeight());
        this.setColor(logTrackerNode.getColor());
        this.setScale(logTrackerNode.getScale());
        this.setContainedText(logTrackerNode.getContainedText());
        this.setNextNodeToRun(logTrackerNode.getNextNodeToRun());
    }

    public LogTrackerNode() {
        super();
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        // The ordering here is Tab < ScrollPane < AnchorPane
        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        TableView<BrowserLog> browserLogTableView = new TableView<>();
        browserLogTableView.setId("browserLogTable-" + getUuidStringWithoutHyphen());

        browserLogTableView.setItems(browserLogList);
        browserLogTableView.getColumns().addAll(new LogDateColumn());
        browserLogTableView.getColumns().addAll(new LogLevelColumn());
        browserLogTableView.getColumns().addAll(new LogMessageColumn());

        browserLogTableView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        UI.setAnchorMargins(browserLogTableView, 0.0, 0.0, 0.0, 0.0);
        browserLogTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Right click context menu
        browserLogTableView.setRowFactory(tableView -> {
            TableRow<BrowserLog> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem removeMenuItem = new MenuItem("Remove");
            MenuItem removeAllMenuItem = new MenuItem("Remove All");

            removeMenuItem.setOnAction(event -> browserLogTableView.getItems().remove(row.getItem()));
            removeAllMenuItem.setOnAction(event -> {
                browserLogTableView.getItems().clear();
            });

            contextMenu.getItems().add(removeMenuItem);
            contextMenu.getItems().add(removeAllMenuItem);

            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });

        VBox vBox = new VBox(5);
        UI.setAnchorMargins(vBox, 50.0, 0.0, 11.0, 0.0);
        vBox.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

        vBox.getChildren().add(browserLogTableView);

        anchorPane.getChildren().add(vBox);

        anchorPane.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        UI.setAnchorMargins(anchorPane, 0.0, 0.0, 0.0, 0.0);

        // Go back to the beginning and run the code to show the tab, it should now exist
        return tab;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void addBrowserLog(BrowserLog browserLog) {
        browserLogList.add(browserLog);
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {

    }
}
