package application.gui.inspect;

import application.gui.UI;
import application.gui.window.InspectWindow;
import application.net.proxy.RecordedRequest;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class ResponseTab extends Tab {
    public ResponseTab(RecordedRequest recordedRequest) {
        this.setText("Response");
        this.setClosable(false);

        TabPane tabPane = new TabPane();
        UI.setAnchorMargins(tabPane, 0.0, 0.0, 0.0, 0.0);
        tabPane.getTabs().add(new RawTab(recordedRequest, InspectWindow.TYPE_RESPONSE));
        tabPane.getTabs().add(new HeadersTab(recordedRequest, InspectWindow.TYPE_RESPONSE));
        tabPane.getTabs().add(new FormattedTab(recordedRequest));
        tabPane.getTabs().add(new RenderTab(recordedRequest));

        this.setContent(tabPane);
    }
}
