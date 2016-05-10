package application.gui;

import javafx.application.Platform;

public class GUIUpdate {
    public static void update(Runnable runnable) {
        Platform.runLater(runnable);
    }
}
