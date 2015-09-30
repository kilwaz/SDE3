package application.test.action;

import application.test.TestParameter;
import org.apache.log4j.Logger;

/**
 * This action switches the current frame that is being referenced as part of the script.
 *
 * The frame can be specified by using the id of the element.
 *
 * select::default can be used to switch to the top frame of the browser.
 */
public class FrameWebAction extends WebAction {
    private static Logger log = Logger.getLogger(FrameWebAction.class);

    public FrameWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter frameToSelectById = getParameterByPath("id");
        TestParameter frameToSelect = getParameterByPath("select");

        if (frameToSelectById.exists()) {
            getDriver().switchTo().frame(frameToSelectById.getParameterValue());
        }

        if (frameToSelect.exists()) {
            getDriver().switchTo().defaultContent();
        }

        refreshCurrentDocument();
    }
}
