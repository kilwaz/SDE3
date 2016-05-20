package application.node.implementations;

import application.data.SavableAttribute;
import application.gui.Controller;
import application.gui.UI;
import application.gui.window.RequestInspectWindow;
import application.net.proxy.GroupedRequests;
import application.net.proxy.RecordedRequest;
import application.node.design.DrawableNode;
import application.node.objects.trackercolumn.*;
import application.utils.Format;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RequestTrackerNode extends DrawableNode {
    private static Logger log = Logger.getLogger(RequestTrackerNode.class);
    private ObservableList<RecordedRequest> requestList = FXCollections.observableArrayList();

    private Label totalRequestsNumber = null;

    // This will make a copy of the node passed to it
    public RequestTrackerNode(RequestTrackerNode requestTrackerNode) {
        this.setX(requestTrackerNode.getX());
        this.setY(requestTrackerNode.getY());
        this.setWidth(requestTrackerNode.getWidth());
        this.setHeight(requestTrackerNode.getHeight());
        this.setColor(requestTrackerNode.getColor());
        this.setScale(requestTrackerNode.getScale());
        this.setContainedText(requestTrackerNode.getContainedText());
        this.setNextNodeToRun(requestTrackerNode.getNextNodeToRun());
    }

    public RequestTrackerNode() {
        super();
    }

    public void addResult(RecordedRequest recordedRequest) {
        class OneShotTask implements Runnable {
            private RecordedRequest recordedRequest;

            private OneShotTask(RecordedRequest recordedRequest) {
                this.recordedRequest = recordedRequest;
            }

            public void run() {
                requestList.add(recordedRequest);
                updateTotalRequests();
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
        return requestList.stream().filter(recordedRequest -> recordedRequest.getUrl().equals(url)).collect(Collectors.toList());
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        TableView<RecordedRequest> requestTableView = new TableView<>();
        requestTableView.setId("requestTable-" + getUuidStringWithoutHyphen());

        requestTableView.setItems(getResultList());
        requestTableView.getColumns().addAll(new RequestNumberColumn());
        requestTableView.getColumns().addAll(new HostColumn());
        requestTableView.getColumns().addAll(new MethodColumn());
        requestTableView.getColumns().addAll(new URLColumn());
        requestTableView.getColumns().addAll(new HasParametersColumn());
        requestTableView.getColumns().addAll(new StatusColumn());
        requestTableView.getColumns().addAll(new RequestLengthColumn());
        requestTableView.getColumns().addAll(new MediaTypeColumn());
        requestTableView.getColumns().addAll(new ExtensionColumn());
        requestTableView.getColumns().addAll(new TitleColumn());
        requestTableView.getColumns().addAll(new SSLColumn());
        requestTableView.getColumns().addAll(new IPColumn());
        requestTableView.getColumns().addAll(new CookieColumn());
        requestTableView.getColumns().addAll(new RequestTimeColumn());
        requestTableView.getColumns().addAll(new DurationColumn());
        requestTableView.getColumns().addAll(new ResponseLengthColumn());
        requestTableView.getColumns().addAll(new RedirectColumn());
        requestTableView.getColumns().addAll(new ProxyColumn());

        requestTableView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

        UI.setAnchorMargins(requestTableView, 0.0, 0.0, 0.0, 0.0);

        requestTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

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

        // Number of requests
        VBox vBox = new VBox(5);
        UI.setAnchorMargins(vBox, 50.0, 0.0, 11.0, 0.0);
        vBox.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

        HBox hbox = new HBox(5);
        Label totalRequests = new Label("Total Requests:");
        totalRequestsNumber = new Label("0");

        hbox.getChildren().add(totalRequests);
        hbox.getChildren().add(totalRequestsNumber);

        vBox.getChildren().add(hbox);
        vBox.getChildren().add(requestTableView);

        anchorPane.getChildren().add(vBox);
        anchorPane.setPrefSize(Integer.MAX_VALUE,Integer.MAX_VALUE);
        UI.setAnchorMargins(anchorPane, 0.0, 0.0, 0.0, 0.0);

        updateTotalRequests();

        return tab;
    }

    public void clearAllRequests() {
        requestList.clear();
        updateTotalRequests();
    }

    public ObservableList<RecordedRequest> getResultList() {
        return requestList;
    }

    public GroupedRequests getGroupedRequests() {
        GroupedRequests recordedRequests = new GroupedRequests();
        recordedRequests.addAll(requestList);
        return recordedRequests;
    }

    private void updateTotalRequests() {
        class GUIUpdate implements Runnable {
            private GUIUpdate() {
            }

            public void run() {
                if (totalRequestsNumber != null) {
                    totalRequestsNumber.setText(Format.get().value(requestList.size()).withCommaSeparator().asString());
                }
            }
        }

        Platform.runLater(new GUIUpdate());
    }
}
