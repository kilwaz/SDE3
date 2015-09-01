package application.gui;

import application.node.design.DrawableNode;
import application.utils.SDEUtils;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AceTextArea extends VBox {
    private DrawableNode node;
    private WebView browser;
    private WebEngine webEngine;
    private VBox instance = this;
    private JSObject jsObject;
    private String textMode;

    private String textToBeSet;
    private static Logger log = Logger.getLogger(AceTextArea.class);

    private static final String EDITOR_HTML = "/aceCodeEditor.html";
    private static final String ACE_JS = "/ace-editor/src/ace.js";
    private static final String LANGUAGE_TOOL_JS = "/ace-editor/src/ext-language_tools.js";

    public AceTextArea(String textMode) {
        this.textMode = textMode;
        init();
    }

    public AceTextArea(DrawableNode node, String textMode) {
        this.node = node;
        this.textMode = textMode;
        init();
    }

    private void init() {
        browser = new WebView();
        webEngine = browser.getEngine();

        String resourcesPath = SDEUtils.getResourcePath();
        String bashEditorPath = resourcesPath + EDITOR_HTML;
        String editorPath = resourcesPath + ACE_JS;
        String languageTools = resourcesPath + LANGUAGE_TOOL_JS;

        String content = "";
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(bashEditorPath));
            content = new String(encoded, "UTF8");
        } catch (IOException ex) {
            log.error(ex);
        }

        // If no logic node is linked then we remove the content placeholder
        if (node != null) {
            content = content.replace("[[content]]", StringEscapeUtils.escapeHtml4(node.getAceTextAreaText()));
        } else {
            content = content.replace("[[content]]", "");
        }

        content = content.replace("[[ace]]", "file:/" + editorPath);
        content = content.replace("[[languageTools]]", "file:/" + languageTools);
        content = content.replace("[[mode]]", textMode);

        webEngine.loadContent(content);

        browser.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(Worker.State.SUCCEEDED)) {
                jsObject = (JSObject) webEngine.executeScript("window");
                jsObject.setMember("java", new Bridge());
                instance.getChildren().add(browser);

                // The reason this is here is because it is possible for the jsobject to not be initialised before calling set text
                // if this happens we need to save what was going to be set and then set it once the jsobject is available
                if (textToBeSet != null) {
                    setText(textToBeSet);
                }
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
                    } catch (UnsupportedFlavorException | IOException ex) {
                        log.error(ex);
                    }
                    jsObject.call("pasteText", pasteText);
                }
            }
        });
    }

    // We need to have a delay here as it is possible when setting the text straight after creating the text area
    // that the jsObject will not be available to run commands on, if that is the case we set the text to
    // textToBeSet and this is updated to the text area once the jsObject is available
    public void setText(String text) {
        if (text != null) {
            if (jsObject == null) {
                textToBeSet = text;
            } else {
                jsObject.call("setText", text);
            }
        }
    }

    public String getText() {
        String text = "";
        if (jsObject != null) {
            text = (String) jsObject.call("getText");
        }

        return text;
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
            if (node != null) {
                node.setAceTextAreaText(value);
            }
        }
    }
}
