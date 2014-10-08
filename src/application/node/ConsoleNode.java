package application.node;

import application.gui.Controller;
import application.gui.SwingNodeConsoleTextArea;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import main.JCTermJavaFx;

public class ConsoleNode extends DrawableNode {
    private TextArea consoleTextArea = new TextArea();
    private SwingNodeConsoleTextArea swingConsoleTextArea;
    private JCTermJavaFx termJavaFx;

    public ConsoleNode(Integer id, Integer programId) {
        super(id, programId);
        //swingConsoleTextArea = new SwingNodeConsoleTextArea();
        //termJavaFx = new JCTermJavaFx();
    }

    public ConsoleNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        //swingConsoleTextArea = new SwingNodeConsoleTextArea();
    }

    public Color getFillColour() {
        return Color.CHOCOLATE;
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
        //tabAnchorPane.getChildren().add(swingConsoleTextArea);
        //tabAnchorPane.getChildren().add(termJavaFx);

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
