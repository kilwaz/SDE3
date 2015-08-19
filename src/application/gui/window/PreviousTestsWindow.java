package application.gui.window;

import application.node.implementations.TestNode;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.net.URL;

public class PreviousTestsWindow extends Stage {
    private TestNode testNode;

    private static Logger log = Logger.getLogger(PreviousTestsWindow.class);


    public PreviousTestsWindow(TestNode testNode) {
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

            this.setScene(new Scene(root, 900, 800));
            this.setTitle("Previous tests - " + testNode.getContainedText());

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            log.error(ex);
        }
    }
}
