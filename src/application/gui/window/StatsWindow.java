package application.gui.window;

import application.error.Error;
import application.gui.UI;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.net.URL;

public class StatsWindow extends Stage {
    private static Logger log = Logger.getLogger(ErrorWindow.class);

    public StatsWindow() {
        init();
    }

    private void init() {
        try {
            AnchorPane statsAnchor = new AnchorPane();

            statsAnchor.setPadding(new Insets(7, 11, 7, 11));

            UI.setAnchorMargins(statsAnchor, 0.0, 0.0, 0.0, 0.0);

            Scene newScene = new Scene(statsAnchor, 900, 800);
            this.setScene(newScene);

            this.setTitle("Stats");

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            Error.CREATE_ERROR_WINDOW.record().create(ex);
        }
    }
}