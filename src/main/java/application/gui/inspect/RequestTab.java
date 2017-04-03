package application.gui.inspect;

import application.gui.UI;
import application.gui.window.InspectWindow;
import application.net.proxy.RecordedRequest;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class RequestTab extends Tab {
    public RequestTab(RecordedRequest recordedRequest) {
        this.setText("Request");
        this.setClosable(false);

        TabPane tabPane = new TabPane();
        UI.setAnchorMargins(tabPane, 0.0, 0.0, 0.0, 0.0);

        tabPane.getTabs().add(new RawTab(recordedRequest, InspectWindow.TYPE_REQUEST));
        tabPane.getTabs().add(new HeadersTab(recordedRequest, InspectWindow.TYPE_REQUEST));
        tabPane.getTabs().add(new ParametersTab(recordedRequest));

        this.setContent(tabPane);
    }
}
