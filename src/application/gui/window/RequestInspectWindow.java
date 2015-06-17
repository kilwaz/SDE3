package application.gui.window;

import application.net.proxy.WebProxyRequest;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashMap;

public class RequestInspectWindow extends Stage {
    private WebProxyRequest webProxyRequest;

    public RequestInspectWindow(WebProxyRequest webProxyRequest) {
        this.webProxyRequest = webProxyRequest;
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

            HashMap<String, String> requestHeaders = webProxyRequest.getRequestHeaders();
            for (String headerName : requestHeaders.keySet()) {
                requestTextArea.appendText(headerName + ": " + requestHeaders.get(headerName) + "\n\r");
            }

            requestTextArea.appendText("\n\r");
            requestTextArea.appendText(webProxyRequest.getRequestContent().replaceAll("(?m)^[ \t]*\r?\n", ""));
            requestTextArea.positionCaret(0);

            // Response Text Area
            TextArea responseTextArea = new TextArea();

            AnchorPane.setBottomAnchor(responseTextArea, 0.0);
            AnchorPane.setLeftAnchor(responseTextArea, 0.0);
            AnchorPane.setRightAnchor(responseTextArea, 0.0);
            AnchorPane.setTopAnchor(responseTextArea, 0.0);

            HashMap<String, String> responseHeaders = webProxyRequest.getResponseHeaders();
            for (String headerName : responseHeaders.keySet()) {
                responseTextArea.appendText(headerName + ": " + responseHeaders.get(headerName) + "\n\r");
            }

            responseTextArea.appendText("\n\r");
            responseTextArea.appendText(webProxyRequest.getResponseContent().replaceAll("(?m)^[ \t]*\r?\n", ""));
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
            this.setTitle(webProxyRequest.getRequestURL());
            this.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
