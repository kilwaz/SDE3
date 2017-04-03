package application.gui;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class UI {
    public static void setAnchorMargins(Node child, Double top, Double bottom, Double left, Double right) {
        if (child != null) {
            AnchorPane.setTopAnchor(child, top);
            AnchorPane.setBottomAnchor(child, bottom);
            AnchorPane.setLeftAnchor(child, left);
            AnchorPane.setRightAnchor(child, right);
        }
    }
}
