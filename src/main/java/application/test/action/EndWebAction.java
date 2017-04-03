package application.test.action;

import org.apache.log4j.Logger;

/**
 * This action immediately ends the current test as soon as it is run.
 * <p>
 * It should be used as a way to end a test in case an error happens.
 */
public class EndWebAction extends WebAction {

    private static Logger log = Logger.getLogger(EndWebAction.class);

    public EndWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        getRunningTest().setContinueTest(false);
    }
}
