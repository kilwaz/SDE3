package application.gui.update.switchnode;

import application.node.objects.Switch;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class RemoveSwitchRow implements Runnable {
    private Switch switchToRemove;
    private VBox switchRows;

    public RemoveSwitchRow(VBox switchRows, Switch switchToRemove) {
        this.switchToRemove = switchToRemove;
        this.switchRows = switchRows;
    }

    @Override
    public void run() {
        Node nodeToRemove = null;
        for (Node node : switchRows.getChildren()) {
            if (node.getId().contains(switchToRemove.getUuidStringWithoutHyphen())) {
                nodeToRemove = node;

            }
        }

        if (nodeToRemove != null) {
            switchRows.getChildren().remove(nodeToRemove);
        }
    }
}