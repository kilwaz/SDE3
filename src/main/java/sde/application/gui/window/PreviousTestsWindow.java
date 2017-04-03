package sde.application.gui.window;

import sde.application.error.Error;
import sde.application.node.implementations.TestNode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import org.apache.log4j.Logger;

import java.net.URL;

public class PreviousTestsWindow extends SDEWindow {
    private TestNode testNode;

    private static Logger log = Logger.getLogger(PreviousTestsWindow.class);


    public PreviousTestsWindow(TestNode testNode) {
        super();
        this.testNode = testNode;
        init();
    }

    private void init() {
        try {
            StackPane root = new StackPane();
            TabPane tabPane = new TabPane();

            Tab request = new Tab();
            request.setText("Request");
            request.setClosable(false);
            tabPane.getTabs().add(request);

            Tab response = new Tab();
            response.setText("Response");
            response.setClosable(false);
            tabPane.getTabs().add(response);

            root.getChildren().add(tabPane);

            createScene(root, 900, 800);
            this.setTitle("Previous tests - " + testNode.getContainedText());

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            Error.CREATE_PREVIOUS_TESTS_WINDOW.record().create(ex);
        }
    }
}
