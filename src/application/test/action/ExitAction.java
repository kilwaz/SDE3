package application.test.action;

import application.utils.ThreadManager;

public class ExitAction extends ActionControl {
    // This class is used to close the web driver which in turn closes the window
    public ExitAction() {
    }

    public void performAction() {
        getDriver().close();
        getDriver().quit();
        getHttpProxyServer().close();
    }
}





