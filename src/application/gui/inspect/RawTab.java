package application.gui.inspect;

import application.gui.UI;
import application.gui.window.InspectWindow;
import application.net.proxy.RecordedHeader;
import application.net.proxy.RecordedRequest;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

import java.util.List;

public class RawTab extends Tab {
    private RecordedRequest recordedRequest;

    public RawTab(RecordedRequest recordedRequest, int type) {
        this.recordedRequest = recordedRequest;
        this.setText("Raw");
        this.setClosable(false);

        // Response Text Area
        TextArea rawContentTextArea = new TextArea();
        rawContentTextArea.setFont(Font.font("Consolas"));
        UI.setAnchorMargins(rawContentTextArea, 0.0, 0.0, 0.0, 0.0);

        // Raw content
        if (type == InspectWindow.TYPE_REQUEST) {
            rawContentTextArea.appendText(createRawRequest());
        } else if (type == InspectWindow.TYPE_RESPONSE) {
            rawContentTextArea.appendText(createRawResponse());
        }
        rawContentTextArea.positionCaret(0);

        AnchorPane rawContentPane = new AnchorPane();
        rawContentPane.getChildren().add(rawContentTextArea);
        this.setContent(rawContentPane);
    }

    public String createRawResponse() {
        StringBuilder responseRaw = new StringBuilder();

        // Response HTTP Status
        responseRaw.append(recordedRequest.getFullStatus() + "\n\r");

        // Response headers
        List<RecordedHeader> responseHeaders = recordedRequest.getResponseHeaders();
        for (RecordedHeader header : responseHeaders) {
            responseRaw.append(header.getName() + ": " + header.getValue() + "\n\r");
        }

        responseRaw.append("\n\r");
        if (recordedRequest.getMediaGroup().equals("image")) {
            responseRaw.append("[image binary data]");
        } else {
            responseRaw.append(recordedRequest.getResponse().replaceAll("(?m)^[ \t]*\r?\n", ""));
        }

        return responseRaw.toString();
    }

    public String createRawRequest() {
        StringBuilder requestRaw = new StringBuilder();

        // Response headers
        List<RecordedHeader> requestHeaders = recordedRequest.getRequestHeaders();
        for (RecordedHeader header : requestHeaders) {
            requestRaw.append(header.getName() + ": " + header.getValue() + "\n\r");
        }

        requestRaw.append("\n\r");
        requestRaw.append(recordedRequest.getRequest().replaceAll("(?m)^[ \t]*\r?\n", ""));

        return requestRaw.toString();
    }
}
