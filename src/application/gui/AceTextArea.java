package application.gui;

import application.Main;
import application.node.design.DrawableNode;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apache.commons.lang3.StringEscapeUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AceTextArea extends VBox {
    private DrawableNode node;
    private WebView browser;
    private WebEngine webEngine;
    private VBox instance = this;
    private JSObject jsObject;

    public AceTextArea(DrawableNode node, String textMode) {
        this.node = node;
        browser = new WebView();
        webEngine = browser.getEngine();

        String resourcesPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            resourcesPath = resourcesPath.replace("SDE3.jar", ""); // Removes the jar name
            resourcesPath = resourcesPath.substring(1); // Removes an initial / from the start of the string
            resourcesPath = URLDecoder.decode(resourcesPath + "../resources", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String bashEditorPath = resourcesPath + "/aceCodeEditor.html";
        String editorPath = resourcesPath + "/ace-editor/src/ace.js";

        String content = "";
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(bashEditorPath));
            content = new String(encoded, "UTF8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        content = content.replace("[[logic]]", StringEscapeUtils.escapeHtml4(node.getAceTextAreaText()));
        content = content.replace("[[ace]]", "file:/" + editorPath);
        content = content.replace("[[mode]]", textMode);

        webEngine.loadContent(content);
        browser.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(Worker.State.SUCCEEDED)) {
                jsObject = (JSObject) webEngine.executeScript("window");
                jsObject.setMember("java", new Bridge());
                instance.getChildren().add(browser);
            }
        });

        browser.setPrefHeight(Integer.MAX_VALUE);
        browser.setPrefWidth(Integer.MAX_VALUE);

        browser.setMaxHeight(Integer.MAX_VALUE);
        browser.setMaxWidth(Integer.MAX_VALUE);

        this.setOnKeyReleased(event -> {
            if (event.isControlDown()) {
                if (event.getCode() == KeyCode.C) {
                    String selectedText = (String) jsObject.call("getSelectedText");
                    StringSelection selection = new StringSelection(selectedText);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                } else if (event.getCode() == KeyCode.V) {
                    String pasteText = "";
                    try {
                        pasteText = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                    } catch (UnsupportedFlavorException | IOException e) {
                        e.printStackTrace();
                    }
                    jsObject.call("pasteText", pasteText);
                }
            }
        });
    }

    public void goToLine(Integer lineNumber) {
        class OneShotTask implements Runnable {
            Integer lineNumber;

            OneShotTask(Integer lineNumber) {
                this.lineNumber = lineNumber;
            }

            public void run() {
                jsObject.call("goToLine", lineNumber);
            }
        }

        Platform.runLater(new OneShotTask(lineNumber));
    }

    // These methods are callable from within javascript using java.methodName
    public final class Bridge {
        public void setTextAreaValue(String value) {
            node.setAceTextAreaText(value);
        }
    }
}
