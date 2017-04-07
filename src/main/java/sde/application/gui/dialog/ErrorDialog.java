package sde.application.gui.dialog;

import sde.application.Main;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ErrorDialog implements Dialog {
    private String title;
    private String header;
    private String content;

    @Override
    public ErrorDialog content(String content) {
        this.content = content;
        return this;
    }

    @Override
    public ErrorDialog title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public ErrorDialog header(String header) {
        this.header = header;
        return this;
    }

    @Override
    public void show() {
        class OneShotTask implements Runnable {
            String title = "";
            String content = "";
            String header = "";

            OneShotTask(String title, String header, String content) {
                this.title = title;
                this.content = content;
                this.header = header;
            }

            public void run() {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(header);
                alert.setContentText(content);
                alert.show();
            }
        }

        if (!Main.isHeadless) {
            Platform.runLater(new OneShotTask(title, header, content));
        }
    }
}
