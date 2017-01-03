package application.gui.inspect;

import application.gui.UI;
import application.gui.columns.inspect.NameColumn;
import application.gui.columns.inspect.HeaderColumn;
import application.net.proxy.RecordedParameter;
import application.net.proxy.RecordedRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;

import java.util.concurrent.atomic.AtomicLong;

public class ParametersTab extends Tab {
    private ObservableList<RecordedParameter> parametersList = FXCollections.observableArrayList();

    public ParametersTab(RecordedRequest recordedRequest) {
        TableView<RecordedParameter> headerTableView = new TableView<>();

        parametersList.addAll(recordedRequest.getRequestParameters());

        this.setText("Parameters (" + parametersList.size() + ")");
        this.setClosable(false);

        headerTableView.getColumns().addAll(new NameColumn());
        headerTableView.getColumns().addAll(new HeaderColumn());
        headerTableView.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        UI.setAnchorMargins(headerTableView, 0.0, 0.0, 0.0, 0.0);
        headerTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        headerTableView.setItems(parametersList);

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
