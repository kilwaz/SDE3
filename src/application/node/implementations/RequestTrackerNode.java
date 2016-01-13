package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.UI;
import application.gui.window.RequestInspectWindow;
import application.net.proxy.RecordedRequest;
import application.node.design.DrawableNode;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RequestTrackerNode extends DrawableNode {
    private ObservableList<RecordedRequest> requestList = FXCollections.observableArrayList();
    private static Logger log = Logger.getLogger(RequestTrackerNode.class);

    // This will make a copy of the node passed to it
    public RequestTrackerNode(RequestTrackerNode requestTrackerNode) {
        this.setX(requestTrackerNode.getX());
        this.setY(requestTrackerNode.getY());
        this.setWidth(requestTrackerNode.getWidth());
        this.setHeight(requestTrackerNode.getHeight());
        this.setColor(requestTrackerNode.getColor());
        this.setScale(requestTrackerNode.getScale());
        this.setContainedText(requestTrackerNode.getContainedText());
//        this.setProgramUuid(requestTrackerNode.getProgramUuid());
        this.setNextNodeToRun(requestTrackerNode.getNextNodeToRun());
    }

    public RequestTrackerNode() {
        super();
    }

    public void addResult(RecordedRequest recordedRequest) {
        class OneShotTask implements Runnable {
            private RecordedRequest recordedRequest;

            OneShotTask(RecordedRequest recordedRequest) {
                this.recordedRequest = recordedRequest;
            }

            public void run() {
                requestList.add(recordedRequest);
            }
        }

        Platform.runLater(new OneShotTask(recordedRequest));
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public List<RecordedRequest> getRequestsByURL(String url) {
        List<RecordedRequest> recordedRequests = requestList.stream().filter(recordedRequest -> recordedRequest.getURL().equals(url)).collect(Collectors.toList());

        return recordedRequests;
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        DecimalFormat formatter = new DecimalFormat("###,###");

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        TableView<RecordedRequest> requestTableView = new TableView<>();
        requestTableView.setId("requestTable-" + getUuidStringWithoutHyphen());

        TableColumn requestID = new TableColumn("ID");
        requestID.setMinWidth(30);
        requestID.setCellValueFactory(new PropertyValueFactory<RecordedRequest, String>("Uuid"));

        TableColumn url = new TableColumn("URL");
        url.setMinWidth(30);
        url.setCellValueFactory(new PropertyValueFactory<RecordedRequest, String>("URL"));

        TableColumn duration = new TableColumn("Duration");
        duration.setMinWidth(30);
        duration.setMaxWidth(120);
        duration.setCellValueFactory(new PropertyValueFactory<RecordedRequest, Integer>("Duration"));
        duration.setCellFactory(column -> new TableCell<RecordedRequest, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
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
        requestSize.setMaxWidth(120);
        requestSize.setCellValueFactory(new PropertyValueFactory<RecordedRequest, Integer>("RequestSize"));
        requestSize.setCellFactory(column -> new TableCell<RecordedRequest, Integer>() {
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

        TableColumn responseSize = new TableColumn("Response Size");
        responseSize.setMinWidth(30);
        responseSize.setMaxWidth(120);
        responseSize.setCellValueFactory(new PropertyValueFactory<RecordedRequest, Integer>("ResponseSize"));
        responseSize.setCellFactory(column -> new TableCell<RecordedRequest, Integer>() {
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

        TableColumn proxy = new TableColumn("Proxy");
        proxy.setMinWidth(30);
        proxy.setCellValueFactory(new PropertyValueFactory<RecordedRequest, String>("ProxyConnectionString"));

        requestTableView.setItems(getResultList());
        requestTableView.getColumns().addAll(requestID);
        requestTableView.getColumns().addAll(url);
        requestTableView.getColumns().addAll(duration);
        requestTableView.getColumns().addAll(requestSize);
        requestTableView.getColumns().addAll(responseSize);
        requestTableView.getColumns().addAll(proxy);
        requestTableView.setLayoutX(11);
        requestTableView.setLayoutY(50);

        requestTableView.setMaxHeight(Integer.MAX_VALUE);
        requestTableView.setMaxWidth(Integer.MAX_VALUE);

        UI.setAnchorMargins(requestTableView, 50.0, 0.0, 11.0, 0.0);

        requestTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Right click context menu
        requestTableView.setRowFactory(tableView -> {
            TableRow<RecordedRequest> row = new TableRow<>();
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

    public ObservableList<RecordedRequest> getResultList() {
        return requestList;
    }
}
