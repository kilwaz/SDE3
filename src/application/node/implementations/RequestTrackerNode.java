package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.Controller;
import application.net.proxy.WebProxyRequest;
import application.node.design.DrawableNode;
import application.gui.window.RequestInspectWindow;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RequestTrackerNode extends DrawableNode {
    private ObservableList<WebProxyRequest> requestList = FXCollections.observableArrayList();

    // This will make a copy of the node passed to it
    public RequestTrackerNode(RequestTrackerNode requestTrackerNode) {
        this.setId(-1);
        this.setX(requestTrackerNode.getX());
        this.setY(requestTrackerNode.getY());
        this.setWidth(requestTrackerNode.getWidth());
        this.setHeight(requestTrackerNode.getHeight());
        this.setColor(requestTrackerNode.getColor());
        this.setScale(requestTrackerNode.getScale());
        this.setContainedText(requestTrackerNode.getContainedText());
        this.setProgramId(requestTrackerNode.getProgramId());
        this.setNextNodeToRun(requestTrackerNode.getNextNodeToRun());
    }

    public RequestTrackerNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public RequestTrackerNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
    }

    public RequestTrackerNode(Double x, Double y, String containedText, Integer id, Integer programId) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, programId, id);
    }

    public void addResult(WebProxyRequest webProxyRequest) {
        class OneShotTask implements Runnable {
            private WebProxyRequest webProxyRequest;

            OneShotTask(WebProxyRequest webProxyRequest) {
                this.webProxyRequest = webProxyRequest;
            }

            public void run() {
                requestList.add(webProxyRequest);
            }
        }

        Platform.runLater(new OneShotTask(webProxyRequest));
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public List<WebProxyRequest> getRequestsByURL(String url) {
        List<WebProxyRequest> webProxyRequests = new ArrayList<>();

        for (WebProxyRequest webProxyRequest : requestList) {
            if (webProxyRequest.getRequestURL().equals(url)) {
                webProxyRequests.add(webProxyRequest);
            }
        }

        return webProxyRequests;
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        DecimalFormat formatter = new DecimalFormat("###,###");

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        TableView<WebProxyRequest> requestTableView = new TableView<>();
        requestTableView.setId("requestTable-" + getId());

        TableColumn requestID = new TableColumn("ID");
        requestID.setMinWidth(30);
        requestID.setMaxWidth(50);
        requestID.setCellValueFactory(new PropertyValueFactory<WebProxyRequest, String>("RequestID"));

        TableColumn url = new TableColumn("URL");
        url.setMinWidth(30);
        url.setCellValueFactory(new PropertyValueFactory<WebProxyRequest, String>("RequestURL"));

        TableColumn duration = new TableColumn("Duration");
        duration.setMinWidth(30);
        duration.setCellValueFactory(new PropertyValueFactory<WebProxyRequest, Long>("RequestDuration"));
        duration.setCellFactory(column -> new TableCell<WebProxyRequest, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null && item > 0) {
                    String formattedItemStr = formatter.format(item);
                    setText(formattedItemStr + " ms");
                } else {
                    setText("Processing...");
                }
            }
        });

        TableColumn requestSize = new TableColumn("Request Size");
        requestSize.setMinWidth(30);
        requestSize.setCellValueFactory(new PropertyValueFactory<WebProxyRequest, String>("RequestContentSize"));

        TableColumn responseSize = new TableColumn("Response Size");
        responseSize.setMinWidth(30);
        responseSize.setCellValueFactory(new PropertyValueFactory<WebProxyRequest, Integer>("ResponseContentSize"));
        responseSize.setCellFactory(column -> new TableCell<WebProxyRequest, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    String formattedItemStr = formatter.format(item);
                    setText(formattedItemStr + " bytes");
                } else {
                    setText("Processing...");
                }
            }
        });

        requestTableView.setItems(getResultList());
        requestTableView.getColumns().addAll(requestID);
        requestTableView.getColumns().addAll(url);
        requestTableView.getColumns().addAll(duration);
        requestTableView.getColumns().addAll(requestSize);
        requestTableView.getColumns().addAll(responseSize);
        requestTableView.setLayoutX(11);
        requestTableView.setLayoutY(50);

        requestTableView.setMaxHeight(Integer.MAX_VALUE);
        requestTableView.setMaxWidth(Integer.MAX_VALUE);
        AnchorPane.setBottomAnchor(requestTableView, 0.0);
        AnchorPane.setLeftAnchor(requestTableView, 11.0);
        AnchorPane.setRightAnchor(requestTableView, 0.0);
        AnchorPane.setTopAnchor(requestTableView, 50.0);
        requestTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Right click context menu
        requestTableView.setRowFactory(tableView -> {
            TableRow<WebProxyRequest> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem inspectMenuItem = new MenuItem("Inspect");
            MenuItem removeMenuItem = new MenuItem("Remove");
            MenuItem removeAllMenuItem = new MenuItem("Remove All");

            inspectMenuItem.setOnAction(event -> new RequestInspectWindow(row.getItem()));
            removeMenuItem.setOnAction(event -> requestTableView.getItems().remove(row.getItem()));
            removeAllMenuItem.setOnAction(event -> requestTableView.getItems().clear());

            contextMenu.getItems().add(inspectMenuItem);
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

        anchorPane.getChildren().add(requestTableView);

        return tab;
    }

    public void clearAllRequests() {
        requestList.clear();
    }

    public ObservableList<WebProxyRequest> getResultList() {
        return requestList;
    }
}
