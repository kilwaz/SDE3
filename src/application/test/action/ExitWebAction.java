package application.test.action;

import org.apache.log4j.Logger;

/**
 * This action closes down the browser, web driver and associated proxy used as this test.
 * <p>
 * It should be used as a clean way to finish a test.
 */
public class ExitWebAction extends WebAction {

    private static Logger log = Logger.getLogger(ExitWebAction.class);

    // This class is used to close the web driver which in turn closes the window
    public ExitWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        // Go through and close each window that is open
//        for (String windowHandle : getDriver().getWindowHandles()) {
//            try {
//                getDriver().switchTo().window(windowHandle);
//                log.info("Closing " + windowHandle);
//                getDriver().quit();
//            } catch (WebDriverException ex) {
//                Error.SELENIUM_FAILED_TO_CLOSE_WINDOW.record().create(ex);
//            }
//        }
        getDriver().quit();
        getHttpProxyServer().close();
    }
}





