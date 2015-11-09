package application.gui.window;

import application.error.Error;
import application.net.proxy.RecordedHeader;
import application.net.proxy.RecordedRequest;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.HashMap;

public class RequestInspectWindow extends Stage {
    private RecordedRequest recordedRequest;

    private static Logger log = Logger.getLogger(RequestInspectWindow.class);

    public RequestInspectWindow(RecordedRequest recordedRequest) {
        this.recordedRequest = recordedRequest;
        init();
    }

    private void init() {
        try {
            StackPane root = new StackPane();

            // Request Test Area
            TextArea requestTextArea = new TextArea();

            AnchorPane.setBottomAnchor(requestTextArea, 0.0);
            AnchorPane.setLeftAnchor(requestTextArea, 0.0);
            AnchorPane.setRightAnchor(requestTextArea, 0.0);
            AnchorPane.setTopAnchor(requestTextArea, 0.0);

            HashMap<String, RecordedHeader> requestHeaders = recordedRequest.getRequestHeaders();
            for (String headerName : requestHeaders.keySet()) {
                requestTextArea.appendText(headerName + ": " + requestHeaders.get(headerName).getValue() + "\n\r");
            }

            requestTextArea.appendText("\n\r");
            requestTextArea.appendText(recordedRequest.getRequest().replaceAll("(?m)^[ \t]*\r?\n", ""));
            requestTextArea.positionCaret(0);

            // Response Text Area
            TextArea responseTextArea = new TextArea();

            AnchorPane.setBottomAnchor(responseTextArea, 0.0);
            AnchorPane.setLeftAnchor(responseTextArea, 0.0);
            AnchorPane.setRightAnchor(responseTextArea, 0.0);
            AnchorPane.setTopAnchor(responseTextArea, 0.0);

            HashMap<String, RecordedHeader> responseHeaders = recordedRequest.getResponseHeaders();
            for (String headerName : responseHeaders.keySet()) {
                responseTextArea.appendText(headerName + ": " + responseHeaders.get(headerName).getValue() + "\n\r");
            }

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

            AnchorPane.setLeftAnchor(requestContent, 0.0);
            AnchorPane.setRightAnchor(requestContent, 0.0);
            AnchorPane.setTopAnchor(requestContent, 0.0);
            AnchorPane.setBottomAnchor(requestContent, 0.0);

            AnchorPane responseContent = new AnchorPane();
            responseContent.getChildren().add(responseTextArea);

            AnchorPane.setLeftAnchor(responseContent, 0.0);
            AnchorPane.setRightAnchor(responseContent, 0.0);
            AnchorPane.setTopAnchor(responseContent, 0.0);
            AnchorPane.setBottomAnchor(responseContent, 0.0);

            response.setContent(responseContent);
            request.setContent(requestContent);

            root.getChildren().add(tabPane);

            this.setScene(new Scene(root, 900, 800));
            this.setTitle(recordedRequest.getURL());

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            Error.CREATE_REQUEST_INSPECT_WINDOW.record().create(ex);
        }
    }
}
