package sde.application.gui.update.switchnode;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AddSwitchRow implements Runnable {
    private HBox hBox;
    private VBox switchRows;

    public AddSwitchRow(VBox switchRows, HBox hBox) {
        this.switchRows = switchRows;
        this.hBox = hBox;
    }

    @Override
    public void run() {
        switchRows.getChildren().add(hBox);
    }
}