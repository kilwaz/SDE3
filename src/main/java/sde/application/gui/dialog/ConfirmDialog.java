package sde.application.gui.dialog;

import sde.application.Main;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class ConfirmDialog implements Dialog {
    private String title;
    private String content;
    private String header;
    private DialogAction yesAction;
    private DialogAction noAction;

    @Override
    public ConfirmDialog content(String content) {
        this.content = content;
        return this;
    }

    @Override
    public ConfirmDialog title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public ConfirmDialog header(String header) {
        this.header = header;
        return this;
    }

    public ConfirmDialog onYesAction(DialogAction yesAction) {
        this.yesAction = yesAction;
        return this;
    }

    public ConfirmDialog onNoAction(DialogAction noAction) {
        this.noAction = noAction;
        return this;
    }

    @Override
    public void show() {
        class OneShotTask implements Runnable {
            String title = "";
            String content = "";

            OneShotTask(String title, String content) {
                this.title = title;
                this.content = content;
            }

            public void run() {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(title);
                alert.setContentText(content);
                alert.setHeaderText(header);
                alert.setGraphic(null);

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == yesButton) {
                    if (yesAction != null) {
                        yesAction.handle();
                    }

                } else if (result.get() == noButton) {
                    if (noAction != null) {
                        noAction.handle();
                    }
                }
            }
        }
        if (!Main.isHeadless) {
            Platform.runLater(new OneShotTask(title, content));
        }
    }
}
