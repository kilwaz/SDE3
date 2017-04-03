package sde.application.test.action;

import sde.application.test.TestParameter;
import org.apache.log4j.Logger;

public class ScreenshotWebAction extends WebAction {
    private static Logger log = Logger.getLogger(ScreenshotWebAction.class);

    public ScreenshotWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter text = getParameterByPath("text");

        if (text.exists()) { // We only run if a function is provided
            takeScreenshotOfPage();
        }
    }
}