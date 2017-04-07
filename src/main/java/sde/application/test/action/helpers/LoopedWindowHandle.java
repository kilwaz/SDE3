package sde.application.test.action.helpers;

public class LoopedWindowHandle extends LoopedObject {
    private String windowHandle = "";

    public LoopedWindowHandle(String windowHandle) {
        this.windowHandle = windowHandle;
    }

    public String getWindowHandle() {
        return windowHandle;
    }
}
