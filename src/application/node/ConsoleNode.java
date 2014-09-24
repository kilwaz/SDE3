package application.node;

import application.gui.Controller;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class ConsoleNode extends DrawableNode {
    private TextArea consoleTextArea = new TextArea();

    public ConsoleNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public ConsoleNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
    }

    public Color getFillColour() {
        return Color.CHOCOLATE;
    }

    public void writeToConsole(String text) {
        class OneShotTask implements Runnable {
            String str;

            OneShotTask(String s) {
                str = s;
            }

            public void run() {
                if (consoleTextArea != null) {
                    consoleTextArea.appendText(str);
                }
            }
        }

        Platform.runLater(new OneShotTask(text));
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = new Tab();
        tab.setText(getContainedText());
        tab.setId(getId().toString());

        AnchorPane tabAnchorPane = new AnchorPane();
        tabAnchorPane.getChildren().add(controller.createNodeNameField(this));
        tabAnchorPane.getChildren().add(controller.createNodeNameLabel());

        AnchorPane.setBottomAnchor(consoleTextArea, 11.0);
        AnchorPane.setLeftAnchor(consoleTextArea, 11.0);
        AnchorPane.setRightAnchor(consoleTextArea, 11.0);
        AnchorPane.setTopAnchor(consoleTextArea, 50.0);

        tabAnchorPane.getChildren().add(consoleTextArea);

        tabAnchorPane.setMaxHeight(Integer.MAX_VALUE);
        tabAnchorPane.setMaxWidth(Integer.MAX_VALUE);
        AnchorPane.setBottomAnchor(tabAnchorPane, 0.0);
        AnchorPane.setLeftAnchor(tabAnchorPane, 0.0);
        AnchorPane.setRightAnchor(tabAnchorPane, 0.0);
        AnchorPane.setTopAnchor(tabAnchorPane, 0.0);

        tab.setContent(tabAnchorPane);

        return tab;
    }

    public String getNodeType() {
        return "ConsoleNode";
    }
}
