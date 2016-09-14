package application.utils.managers;

import application.gui.window.SDEWindow;

import java.util.ArrayList;
import java.util.List;

public class WindowManager {
    private static WindowManager instance;
    private List<SDEWindow> windows = new ArrayList<>();

    public WindowManager() {
        instance = this;
    }

    public static WindowManager getInstance() {
        if (instance == null) {
            instance = new WindowManager();
        }

        return instance;
    }

    public void addWindow(SDEWindow window) {
        windows.add(window);
    }

    public void closeAllWindows() {
        for (SDEWindow window : windows) {
            window.close();
        }
    }
}
