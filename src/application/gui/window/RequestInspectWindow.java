package application.gui.window;

import application.error.Error;
import application.gui.UI;
import application.net.proxy.MetaRecordedRequest;
import application.net.proxy.RecordedHeader;
import application.net.proxy.RecordedRequest;
import application.utils.managers.WindowManager;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.List;

public class RequestInspectWindow extends SDEWindow {
    private static Logger log = Logger.getLogger(RequestInspectWindow.class);
    private RecordedRequest recordedRequest;

    public RequestInspectWindow(MetaRecordedRequest metaRecordedRequest) {
        super();
        this.recordedRequest = metaRecordedRequest.getRecordedRequest();
        init();
    }

    private void init() {
        try {
            StackPane root = new StackPane();

            // Request Test Area
            TextArea requestTextArea = new TextArea();
            UI.setAnchorMargins(requestTextArea, 0.0, 0.0, 0.0, 0.0);

            // Request headers
            List<RecordedHeader> requestHeaders = recordedRequest.getRequestHeaders();
            for (RecordedHeader header : requestHeaders) {
                requestTextArea.appendText(header.getName() + ": " + header.getValue() + "\n\r");
            }

            // Request content
            requestTextArea.appendText("\n\r");
            requestTextArea.appendText(recordedRequest.getRequest().replaceAll("(?m)^[ \t]*\r?\n", ""));
            requestTextArea.positionCaret(0);

            // Response Text Area
            TextArea responseTextArea = new TextArea();
            UI.setAnchorMargins(responseTextArea, 0.0, 0.0, 0.0, 0.0);

            // Response HTTP Status
            responseTextArea.appendText(recordedRequest.getFullStatus() + "\n\r");

            // Response headers
            List<RecordedHeader> responseHeaders = recordedRequest.getResponseHeaders();
            for (RecordedHeader header : responseHeaders) {
                responseTextArea.appendText(header.getName() + ": " + header.getValue() + "\n\r");
            }

            // Response content
            responseTextArea.appendText("\n\r");
            responseTextArea.appendText(recordedRequest.getResponse().replaceAll("(?m)^[ \t]*\r?\n", ""));
            responseTextArea.positionCaret(0);

            TabPane tabPane = new TabPane();

            Tab request = new Tab();
            request.setText("Request");
            request.setClosable(false);
            tabPane.getTabs().add(request);

            Tab response = new Tab();
            response.setText("Response");
            response.setClosable(false);
            tabPane.getTabs().add(response);

            AnchorPane requestContent = new AnchorPane();
            requestContent.getChildren().add(requestTextArea);

            UI.setAnchorMargins(requestContent, 0.0, 0.0, 0.0, 0.0);

            AnchorPane responseContent = new AnchorPane();
            responseContent.getChildren().add(responseTextArea);

            UI.setAnchorMargins(responseContent, 0.0, 0.0, 0.0, 0.0);

            response.setContent(responseContent);
            request.setContent(requestContent);

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
