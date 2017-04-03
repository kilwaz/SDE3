package sde.application.gui;

import javafx.scene.control.TextField;

public class SDETextField extends TextField {
    public static void setToSaved(TextField textField) {
        textField.setStyle("-fx-background-color: lightgreen;");
    }

    public static void setToChanged(TextField textField) {
        textField.setStyle("-fx-background-color: yellow;");
    }
}
