package application.gui;

import application.node.SourceNode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SourceTextArea extends VBox {
    private SourceNode sourceNode;
    private WebView browser;
    private WebEngine webEngine;
    private VBox instance = this;

    public SourceTextArea(SourceNode sourceNode) {
        this.sourceNode = sourceNode;
        browser = new WebView();
        webEngine = browser.getEngine();

        URL editorURL = getClass().getResource("/ace-editor/src/ace.js");
        URL sourceEditorURL = getClass().getResource("/sourceEditor.html");

        String content = "";
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(sourceEditorURL.toExternalForm().replaceFirst("file:/", "")));
            content = new String(encoded, "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        content = content.replace("[[source]]", sourceNode.getSource().getSource());
        content = content.replace("[[aceSource]]", editorURL.toExternalForm());

        webEngine.loadContent(content);
        browser.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue.equals(Worker.State.SUCCEEDED)) {
                    JSObject jsobj = (JSObject) webEngine.executeScript("window");
                    jsobj.setMember("java", new Bridge());
                    instance.getChildren().add(browser);
                }
            }
        });
    }

    public final class Bridge {
        public void test(String value) {
            sourceNode.getSource().setSource(value);
        }
    }
}
