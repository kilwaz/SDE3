package application.gui.inspect;

import application.gui.AceTextArea;
import application.net.proxy.RecordedRequest;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;

import java.util.HashMap;

public class FormattedTab extends Tab {
    private static final HashMap<String, String> fileTypeToAceEditor = new HashMap<>();

    static {
        fileTypeToAceEditor.put("javascript", "javascript");
        fileTypeToAceEditor.put("html", "html");
        fileTypeToAceEditor.put("json", "json");
        fileTypeToAceEditor.put("css", "css");
    }

    public FormattedTab(RecordedRequest recordedRequest) {
        this.setText("Formatted");
        this.setClosable(false);

        if (fileTypeToAceEditor.containsKey(recordedRequest.getMediaSubType())) {
            AceTextArea exportTextArea = new AceTextArea("ace/mode/" + fileTypeToAceEditor.get(recordedRequest.getMediaSubType()), recordedRequest.getResponse());
            this.setContent(exportTextArea);
        } else {
            StackPane stackPane = new StackPane();
            Label label = new Label("Nothing to show");
            stackPane.getChildren().add(label);
            StackPane.setAlignment(label, Pos.CENTER);
            this.setContent(stackPane);
        }
    }
}
