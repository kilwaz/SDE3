package application.node;

import application.gui.Controller;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class ConsoleNode extends DrawableNode {
    private TextArea consoleTextArea = new TextArea();

    // This will make a copy of the node passed to it
    public ConsoleNode(ConsoleNode consoleNode) {
        this.setId(-1);
        this.setX(consoleNode.getX());
        this.setY(consoleNode.getY());
        this.setWidth(consoleNode.getWidth());
        this.setHeight(consoleNode.getHeight());
        this.setColor(consoleNode.getColor());
        this.setScale(consoleNode.getScale());
        this.setContainedText(consoleNode.getContainedText());
        this.setProgramId(consoleNode.getProgramId());
        this.setNextNodeToRun(consoleNode.getNextNodeToRun());
    }

    public ConsoleNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public ConsoleNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
    }

    private String consoleToWrite = "";

    public void writeToConsole(String text) {
        consoleToWrite += text;
        class OneShotTask implements Runnable {
            OneShotTask() {
            }

            public void run() {
                if (consoleTextArea != null) {
                    consoleTextArea.appendText(consoleToWrite);
                    consoleToWrite = "";
                }
            }
        }

        Platform.runLater(new OneShotTask());
    }

    public void clearConsole() {
        consoleTextArea.clear();
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        AnchorPane.setBottomAnchor(consoleTextArea, 11.0);
        AnchorPane.setLeftAnchor(consoleTextArea, 11.0);
        AnchorPane.setRightAnchor(consoleTextArea, 11.0);
        AnchorPane.setTopAnchor(consoleTextArea, 50.0);

        anchorPane.getChildren().add(consoleTextArea);

        MenuItem menuItemNewProgram = new MenuItem("Clear All");
        menuItemNewProgram.setOnAction(event1 -> {
            clearConsole();
        });

        ContextMenu clearTextAreaContextMenu = new ContextMenu();
        clearTextAreaContextMenu.getItems().add(menuItemNewProgram);

        consoleTextArea.setContextMenu(clearTextAreaContextMenu);

        return tab;
    }
}
