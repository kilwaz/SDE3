package sde.application.gui.dialog;

import sde.application.GUI;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionDialog implements Dialog {
    private String title;
    private String content;
    private String header;
    private Exception ex;

    public ExceptionDialog exception(Exception ex) {
        this.ex = ex;
        return this;
    }

    public ExceptionDialog content(String content) {
        this.content = content;
        return this;
    }

    public ExceptionDialog title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public ExceptionDialog header(String header) {
        this.header = header;
        return this;
    }

    @Override
    public void show() {
        class OneShotTask implements Runnable {
            Exception ex;
            String title = "";
            String content = "";

            OneShotTask(String title, String content, Exception ex) {
                this.title = title;
                this.content = content;
                this.ex = ex;
            }

            public void run() {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setContentText(content);
                alert.setHeaderText(header);
                alert.setGraphic(null);

                // Create expandable Exception.
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                String exceptionText = sw.toString();

                Label label = new Label("The exception stacktrace was:");

                TextArea textArea = new TextArea(exceptionText);
                textArea.setEditable(false);
                textArea.setWrapText(true);

                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);

                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(label, 0, 0);
                expContent.add(textArea, 0, 1);

                // Set expandable Exception into the dialog pane.
                alert.getDialogPane().setExpandableContent(expContent);
                alert.show();
            }
        }
        if (!GUI.isHeadless) {
            Platform.runLater(new OneShotTask(title, content, ex));
        }
    }
}
