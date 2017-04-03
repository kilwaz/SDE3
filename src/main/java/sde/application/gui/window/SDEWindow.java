package sde.application.gui.window;

import sde.application.utils.managers.WindowManager;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

public class SDEWindow extends Stage {
    private static Logger log = Logger.getLogger(SDEWindow.class);

    public SDEWindow() {
        super();
        WindowManager.getInstance().addWindow(this);
    }

    protected void createScene(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        this.setScene(scene);
        this.setOnCloseRequest(we -> WindowManager.getInstance().removeWindow((SDEWindow) we.getSource()));
    }
}
