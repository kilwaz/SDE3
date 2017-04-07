package sde.application.gui.window;

import sde.application.data.export.har.HarExportHelper;
import sde.application.error.Error;
import sde.application.gui.UI;
import sde.application.net.proxy.MetaRecordedRequest;
import sde.application.net.proxy.ProxyRequestListener;
import sde.application.net.proxy.RecordedRequest;
import sde.application.net.proxy.snoop.HttpProxyServer;
import sde.application.utils.BrowserHelper;
import sde.application.utils.Format;
import sde.application.utils.SDEThread;
import com.jayway.awaitility.Awaitility;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import sde.application.gui.columns.requesttracker.*;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ManualProxyWindow extends SDEWindow implements ProxyRequestListener {
    private static Logger log = Logger.getLogger(InspectWindow.class);

    private ObservableList<MetaRecordedRequest> requestList = FXCollections.observableArrayList();
    private Label totalRequestsNumber = null;
    private HttpProxyServer httpProxyServer;

    public ManualProxyWindow() {
        super();
        init();
    }

    private void init() {
        try {
            AnchorPane root = new AnchorPane();
            HBox hBox = new HBox(5);
            VBox vBox = new VBox(5);
            UI.setAnchorMargins(vBox, 5.0, 5.0, 5.0, 5.0);

            Button openBrowser = new Button("Open Browser");
            openBrowser.setOnAction(event -> {
                WebDriver driver = BrowserHelper.getChrome(httpProxyServer.getConnectionString());
                driver.get("about:blank"); // Opens blank page

            });

            Button harOutput = new Button("Export HAR");
            harOutput.setOnAction(event -> {
                HarExportHelper.build().withRequests(requestList).export();
            });

            httpProxyServer = new HttpProxyServer();
            SDEThread webProxyThread = new SDEThread(httpProxyServer, "Running manual proxy server", null, true);
            Awaitility.await().atMost(60000, TimeUnit.MILLISECONDS).until(httpProxyServer.nowConnected());
            httpProxyServer.addRequestListener(this);

            this.setOnCloseRequest(event -> {
                if (httpProxyServer != null) {
                    httpProxyServer.close();
                }
            });

            UI.setAnchorMargins(root, 0.0, 0.0, 0.0, 0.0);

            TableView<MetaRecordedRequest> requestTableView = new TableView<>();
            requestTableView.setItems(getResultList());
            requestTableView.getColumns().addAll(new RequestNumberColumn());
            requestTableView.getColumns().addAll(new HostColumn());
            requestTableView.getColumns().addAll(new MethodColumn());
            requestTableView.getColumns().addAll(new URLColumn());
            requestTableView.getColumns().addAll(new HasParametersColumn());
            requestTableView.getColumns().addAll(new StatusColumn());
            requestTableView.getColumns().addAll(new MediaTypeColumn());
            requestTableView.getColumns().addAll(new ExtensionColumn());
            requestTableView.getColumns().addAll(new TitleColumn());
            requestTableView.getColumns().addAll(new SSLColumn());
            requestTableView.getColumns().addAll(new IPColumn());
            requestTableView.getColumns().addAll(new CookieColumn());
            requestTableView.getColumns().addAll(new RequestTimeColumn());
            requestTableView.getColumns().addAll(new DurationColumn());
            requestTableView.getColumns().addAll(new RequestLengthColumn());
            requestTableView.getColumns().addAll(new ResponseLengthColumn());
            requestTableView.getColumns().addAll(new RedirectColumn());
            requestTableView.getColumns().addAll(new ProxyColumn());

            requestTableView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
            UI.setAnchorMargins(requestTableView, 0.0, 0.0, 0.0, 0.0);
            requestTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

            // Right click context menu
            requestTableView.setRowFactory(tableView -> {
                TableRow<MetaRecordedRequest> row = new TableRow<>();
                ContextMenu contextMenu = new ContextMenu();
                MenuItem inspectMenuItem = new MenuItem("Inspect");
                MenuItem removeMenuItem = new MenuItem("Remove");
                MenuItem removeAllMenuItem = new MenuItem("Remove All");

                inspectMenuItem.setOnAction(event -> new InspectWindow(row.getItem()));
                removeMenuItem.setOnAction(event -> requestTableView.getItems().remove(row.getItem()));
                removeAllMenuItem.setOnAction(event -> {
                    requestTableView.getItems().clear();
                    //updateTotalRequests();
                });

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

            Label totalRequests = new Label("Total Requests:");
            totalRequestsNumber = new Label("0");

            hBox.getChildren().add(openBrowser);
            hBox.getChildren().add(harOutput);
            hBox.getChildren().add(totalRequests);
            hBox.getChildren().add(totalRequestsNumber);
            vBox.getChildren().add(hBox);
            vBox.getChildren().add(requestTableView);
            root.getChildren().add(vBox);


            URL url = getClass().getResource("/icon.png");
            createScene(root, 900, 800);
            this.getIcons().add(new Image(url.toExternalForm()));
            this.setTitle("Manual Proxy - Listening on " + httpProxyServer.getConnectionString());

            this.show();
        } catch (Exception ex) {
            Error.CREATE_REQUEST_INSPECT_WINDOW.record().create(ex);
        }
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

    public ObservableList<MetaRecordedRequest> getResultList() {
        return requestList;
    }

    @Override
    public void addRequest(RecordedRequest recordedRequest) {
        class OneShotTask implements Runnable {
            private RecordedRequest recordedRequest;

            private OneShotTask(RecordedRequest recordedRequest) {
                this.recordedRequest = recordedRequest;
            }

            public void run() {
                requestList.add(recordedRequest.getMetaRecordedRequest());
                updateTotalRequests();
            }
        }

        Platform.runLater(new OneShotTask(recordedRequest));
    }
}
