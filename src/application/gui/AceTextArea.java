package application.gui;

import application.error.Error;
import application.node.design.DrawableNode;
import application.utils.CompileLineError;
import application.utils.SDEUtils;
import application.utils.managers.JobManager;
import application.utils.timers.CompileLogicCodeJob;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.quartz.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

public class AceTextArea extends VBox {
    private static final String EDITOR_HTML = "/aceCodeEditor.html";
    private static final String ACE_JS = "/ace-editor/src/ace.js";
    private static final String LANGUAGE_TOOL_JS = "/ace-editor/src/ext-language_tools.js";
    private static Logger log = Logger.getLogger(AceTextArea.class);
    private DrawableNode node;
    private WebView browser;
    private WebEngine webEngine;
    private AceTextArea instance = this;
    private JSObject jsObject;
    private String textMode;
    private Boolean initialised = false;
    private String textToBeSet;
    private String initialContent = null;
    private Boolean beautify = false;
    private Boolean waitingToTriggerCompile = false;
    private JobKey currentCompileJobKey = null;

    public AceTextArea(String textMode) {
        this.textMode = textMode;
        init();
    }

    public AceTextArea(String textMode, String initialContent) {
        this.textMode = textMode;
        this.initialContent = initialContent;
        this.beautify = true;
        init();
    }

    public AceTextArea(String textMode, Boolean init) {
        this.textMode = textMode;
        if (init) {
            init();
        }
    }

    public AceTextArea(DrawableNode node, String textMode) {
        this.node = node;
        this.textMode = textMode;
        init();
    }

    public AceTextArea(DrawableNode node, String textMode, Boolean init) {
        this.node = node;
        this.textMode = textMode;
        if (init) {
            init();
        }
    }

    public void initOnGUIThread(AnchorPane runningAnchorPane) {
        class GUIUpdate implements Runnable {
            private AnchorPane runningAnchorPane;

            GUIUpdate(AnchorPane runningAnchorPane) {
                this.runningAnchorPane = runningAnchorPane;
            }

            public void run() {
                init();
                runningAnchorPane.getChildren().add(getThisTextArea());
            }
        }

        Platform.runLater(new GUIUpdate(runningAnchorPane));
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
            Error.ACE_TEXT_PASTE.record().create(ex);
        }

        content = content.replace("[[ace]]", "file:/" + editorPath);
        content = content.replace("[[languageTools]]", "file:/" + languageTools);
        content = content.replace("[[mode]]", textMode);

        // If no logic node is linked then we remove the content placeholder
        if (node != null) {
            content = content.replace("[[content]]", StringEscapeUtils.escapeHtml4(node.getAceTextAreaText()));
        } else if (initialContent != null) {
            content = content.replace("[[content]]", StringEscapeUtils.escapeHtml4(initialContent));
        } else {
            content = content.replace("[[content]]", "");
        }

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

//        this.setOnKeyReleased(event -> {
//            if (event.isControlDown()) {
//                if (event.getCode() == KeyCode.C) {
//                    log.info("Copying text!");
//                    String selectedText = (String) jsObject.call("getSelectedText");
//                    StringSelection selection = new StringSelection(selectedText);
//                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//                    clipboard.setContents(selection, selection);
//                    event.consume();
//                } else if (event.getCode() == KeyCode.V) {
//                    log.info("Pasting text!");
//                    String pasteText = "";
//                    try {
//                        pasteText = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
//                        log.info("Paste text is " + pasteText);
//                    } catch (UnsupportedFlavorException | IOException ex) {
//                        Error.ACE_TEXT_PASTE.record().create(ex);
//                    }
//                    jsObject.call("pasteText", pasteText);
//                    event.consume();
//                }
//            }
//        });

        browser.setPrefHeight(Integer.MAX_VALUE);
        browser.setPrefWidth(Integer.MAX_VALUE);

        browser.setMaxHeight(Integer.MAX_VALUE);
        browser.setMaxWidth(Integer.MAX_VALUE);

        webEngine.setOnAlert(event -> {
            log.info("Alert from webview = " + event.getData());
        });

        initialised = true;
    }

    public String getText() {
        String text = "";
        if (jsObject != null) {
            text = (String) jsObject.call("getText");
        }

        return text;
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

    public void setCompileErrors(List<CompileLineError> compileLineErrors) {
        class OneShotTask implements Runnable {
            List<CompileLineError> compileLineErrors;

            private OneShotTask(List<CompileLineError> compileLineErrors) {
                this.compileLineErrors = compileLineErrors;
            }

            public void run() {
                String errorArrayStr = "[{}]";
                if (compileLineErrors.size() > 0) {
                    StringBuilder errorArrayBuilder = new StringBuilder();
                    errorArrayBuilder.append("[");
                    Boolean firstElement = true;
                    for (CompileLineError compileLineError : compileLineErrors) {
                        if (!firstElement) {
                            errorArrayBuilder.append(",");
                        }
                        errorArrayBuilder.append("{row: '").append(compileLineError.getLineNumber() - 1).append("', text: '").append(compileLineError.getOutError().replace("'", "\\'"));
                        errorArrayBuilder.append("' ,type: 'error'}");
                        firstElement = false;
                    }
                    errorArrayBuilder.append("]");
                    errorArrayStr = errorArrayBuilder.toString();
                }

                if (jsObject != null) {
                    jsObject.call("setAnnotation", errorArrayStr);
                }
            }
        }

        Platform.runLater(new OneShotTask(compileLineErrors));
    }

    public void beautify() {
        if (jsObject != null) {
            jsObject.call("beautify");
        }
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

    public Boolean isInitialised() {
        return initialised;
    }

    private AceTextArea getThisTextArea() {
        return this;
    }

    public void setWaitingToTriggerCompile(Boolean waitingToTriggerCompile) {
        this.waitingToTriggerCompile = waitingToTriggerCompile;
    }

    public DrawableNode getNode() {
        return node;
    }

    // These methods are callable from within javascript using java.methodName
    public final class Bridge {
        public void setTextAreaValue(String value) {
            if (node != null) {
                node.setAceTextAreaText(value);
            }

            if (!waitingToTriggerCompile) {
                waitingToTriggerCompile = true;
            } else {
                JobManager.getInstance().stopJob(currentCompileJobKey);
            }

            // Starts a job to compile the code after 1 second of inactivity
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("aceTextArea", instance);

            JobDetail compileCodeJob = JobBuilder.newJob(CompileLogicCodeJob.class).usingJobData(jobDataMap).build();
            currentCompileJobKey = compileCodeJob.getKey();
            SimpleScheduleBuilder compileCodeSimpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
            TriggerBuilder compileCodeTriggerBuilder = TriggerBuilder.newTrigger();
            long startTime = System.currentTimeMillis() + 1000L; // One second from now
            compileCodeTriggerBuilder.startAt(new Date(startTime));

            JobManager.getInstance().scheduleJob(compileCodeJob, compileCodeTriggerBuilder.withSchedule(compileCodeSimpleScheduleBuilder).build());
        }
    }
}
