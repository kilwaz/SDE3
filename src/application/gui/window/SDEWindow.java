package application.gui.window;

import application.utils.managers.WindowManager;
import javafx.stage.Stage;

public class SDEWindow extends Stage {
    public SDEWindow() {
        WindowManager.getInstance().addWindow(this);
    }
}
