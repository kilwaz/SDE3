package application;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;

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
        String content = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <style type=\"text/css\" media=\"screen\">\n" +
                "        #editor {\n" +
                "            position: absolute;\n" +
                "            top: 0;\n" +
                "            right: 0;\n" +
                "            bottom: 0;\n" +
                "            left: 0;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<div id=\"editor\">" + sourceNode.getSource().getSource() + "</div>\n" +
                "\n" +
                "<script src=\"" + editorURL.toExternalForm() + "\" type=\"text/javascript\" charset=\"utf-8\"></script>\n" +
                "<script>\n" +
                "    var editor = ace.edit(\"editor\");\n" +
                "    editor.setTheme(\"ace/theme/tomorrow\");\n" +
                "    editor.getSession().setMode(\"ace/mode/java\");\n" +
                "    editor.on('change', function() {\n" +
                "        java.test(editor.getValue());\n" +
                "    });\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";

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
