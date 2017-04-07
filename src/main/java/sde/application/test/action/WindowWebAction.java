package sde.application.test.action;

import sde.application.test.TestParameter;
import sde.application.test.action.helpers.LoopedWindowHandle;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This action is used to open a new window or to switch the current window or tab
 */
public class WindowWebAction extends WebAction {

    private static Logger log = Logger.getLogger(WindowWebAction.class);

    public WindowWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter windowHandle = getParameterByPath("handle");
        TestParameter newWindow = getParameterByPath("new");
        TestParameter switchWindowByTitle = getParameterByPath("switch::title");
        TestParameter switchWindowByLoop = getParameterByPath("switch::loop");

        if (newWindow.exists()) {
            if ("tab".equals(newWindow.getParameterValue())) {
                getDriver().findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL + "t");
            }
        }

        if (windowHandle.exists()) {
            Set<String> handles = getDriver().getWindowHandles();
            //log.info("Total handles = " + handles.size());

            String currentWindow = getDriver().getWindowHandle();

            for (String windowHandleStr : handles) {
                getDriver().switchTo().window(windowHandleStr);
//                if (currentWindow.equals(windowHandleStr)) {
//                    log.info("CURRENT: " + getDriver().getWindowHandle() + " " + getDriver().getTitle());
//                } else {
//                    log.info(getDriver().getWindowHandle() + " " + getDriver().getTitle());
//                }
            }

            getDriver().switchTo().window(currentWindow);
        }

        if (switchWindowByLoop.exists()) {
            if (getLoopTracker().getLoop(switchWindowByLoop.getParameterValue()) != null) {
                LoopedWindowHandle loopedWindowHandle = (LoopedWindowHandle) getLoopTracker().getLoop(switchWindowByLoop.getParameterValue()).getCurrentLoopObject();
                if (loopedWindowHandle != null) {
                    //log.info("Switching to window " + loopedWindowHandle.getWindowHandle());
                    try {
                        // We only wait for 10 seconds for the browser to find the window
                        getDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
                        getDriver().switchTo().window(loopedWindowHandle.getWindowHandle());
                    } finally {
                        // We sent the driver back to being unlimited timeout for page loads
                        getDriver().manage().timeouts().pageLoadTimeout(-1, TimeUnit.SECONDS);
                    }
                }
            }
        }

        if (switchWindowByTitle.exists()) { // Try to switch to window via title match
            Set<String> handles = getDriver().getWindowHandles();
            String currentWindow = getDriver().getWindowHandle();

            Boolean foundWindow = false;
            for (String windowHandleStr : handles) {
                getDriver().switchTo().window(windowHandleStr);
                String title = getDriver().getTitle();
                if (title.equals(switchWindowByTitle.getParameterValue())) {
                    foundWindow = true;
                    break;
                }
            }

            if (!foundWindow) {
                log.info("Unable to switch to window with title " + switchWindowByTitle.getParameterValue());
                getDriver().switchTo().window(currentWindow);
            }
        }
    }
}
