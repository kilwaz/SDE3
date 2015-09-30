package application.test.action;

/**
 * This action closes down the browser, web driver and associated proxy used as this test.
 *
 * It should be used as a clean way to finish a test.
 */
public class ExitWebAction extends WebAction {
    // This class is used to close the web driver which in turn closes the window
    public ExitWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        getDriver().close();
        getDriver().quit();
        getHttpProxyServer().close();
    }
}





