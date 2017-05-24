package sde.application.gui.inspect;

import sde.application.gui.UI;
import sde.application.gui.columns.inspect.NameColumn;
import sde.application.gui.columns.inspect.HeaderColumn;
import sde.application.gui.window.InspectWindow;
import sde.application.net.proxy.RecordedHeader;
import sde.application.net.proxy.RecordedRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;

import java.util.concurrent.atomic.AtomicLong;

public class HeadersTab extends Tab {
    private ObservableList<RecordedHeader> headerList = FXCollections.observableArrayList();

    public HeadersTab(RecordedRequest recordedRequest, int type) {
        TableView<RecordedHeader> headerTableView = new TableView<>();

        this.setClosable(false);

        if (type == InspectWindow.TYPE_REQUEST) {
            this.setText("Headers (" + recordedRequest.getRequestHeaders().size() + ")");
            headerList.addAll(recordedRequest.getRequestHeaders());
        } else if (type == InspectWindow.TYPE_RESPONSE) {
            this.setText("Headers (" + recordedRequest.getResponseHeaders().size() + ")");
            headerList.addAll(recordedRequest.getResponseHeaders());
        }

        headerTableView.getColumns().addAll(new NameColumn());
        headerTableView.getColumns().addAll(new HeaderColumn());
        headerTableView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        UI.setAnchorMargins(headerTableView, 0.0, 0.0, 0.0, 0.0);
        headerTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        headerTableView.setItems(headerList);

        //headerTableView.setColumnResizePolicy((param) -> true );
        Platform.runLater(() -> customResize(headerTableView));

        this.setContent(headerTableView);
    }

    private void customResize(TableView<?> view) {
        AtomicLong width = new AtomicLong();
        view.getColumns().forEach(col -> {
            width.addAndGet((long) col.getWidth());
        });
        double tableWidth = view.getWidth();

        if (tableWidth > width.get()) {
            view.getColumns().forEach(col -> {
                col.setPrefWidth(col.getWidth() + ((tableWidth - width.get()) / view.getColumns().size()));
            });
        }
    }
}
