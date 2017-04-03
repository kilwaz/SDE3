package sde.application.gui.window;

import sde.application.error.Error;
import sde.application.gui.inspect.RequestTab;
import sde.application.gui.inspect.ResponseTab;
import sde.application.net.proxy.MetaRecordedRequest;
import sde.application.net.proxy.RecordedRequest;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import org.apache.log4j.Logger;

import java.net.URL;

public class InspectWindow extends SDEWindow {
    public static final int TYPE_REQUEST = 0;
    public static final int TYPE_RESPONSE = 1;

    private static Logger log = Logger.getLogger(InspectWindow.class);
    private RecordedRequest recordedRequest;

    public InspectWindow(MetaRecordedRequest metaRecordedRequest) {
        super();
        this.recordedRequest = metaRecordedRequest.getRecordedRequest();
        init();
    }

    private void init() {
        try {
            StackPane root = new StackPane();

            // Tabs
            TabPane tabPane = new TabPane();
            tabPane.getTabs().add(new RequestTab(recordedRequest));
            tabPane.getTabs().add(new ResponseTab(recordedRequest));

            root.getChildren().add(tabPane);

            createScene(root, 900, 800);
            this.setTitle(recordedRequest.getUrl());

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            Error.CREATE_REQUEST_INSPECT_WINDOW.record().create(ex);
        }
    }
}
