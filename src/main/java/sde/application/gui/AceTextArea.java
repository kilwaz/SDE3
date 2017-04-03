package sde.application.gui;

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
import sde.application.error.Error;
import sde.application.node.design.DrawableNode;
import sde.application.utils.CompileLineError;
import sde.application.utils.SDEUtils;
import sde.application.utils.managers.JobManager;
import sde.application.utils.timers.CompileLogicCodeJob;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AceTextArea extends VBox {
    private static final String EDITOR_HTML = "/aceCodeEditor.html";
    private static final String ACE_JS = "/src/ace.js";
    private static final String LANGUAGE_TOOL_JS = "/src/ext-language_tools.js";
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
    private Bridge webBridge = null;

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
        String editorPath = resourcesPath + (SDEUtils.isJar() ? "!" : "") + ACE_JS;
        String languageTools = resourcesPath + (SDEUtils.isJar() ? "!" : "") + LANGUAGE_TOOL_JS;

        String content = "";

        InputStream in = getClass().getResourceAsStream(EDITOR_HTML);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        content = reader.lines().collect(Collectors.joining());

        String fileStart = "";

        if (SDEUtils.isJar()) {
            fileStart = "jar:file:/";
        } else {
            fileStart = "file:/";
        }

        content = content.replace("[[ace]]", fileStart + editorPath);
        content = content.replace("[[languageTools]]", fileStart + languageTools);
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

        webBridge = new Bridge();

        browser.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(Worker.State.SUCCEEDED)) {
                jsObject = (JSObject) webEngine.executeScript("window");
                jsObject.setMember("java", webBridge);
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

        webEngine.setOnError(event -> {
            Error.ACE_EDITOR_ERROR.record().additionalInformation(event.getMessage()).create();
        });

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
