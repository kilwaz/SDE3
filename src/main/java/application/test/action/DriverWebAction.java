package application.test.action;

import org.apache.log4j.Logger;

/**
 * This action specifies what browser the test should be run with.
 */
public class DriverWebAction extends WebAction {
    private static Logger log = Logger.getLogger(DriverWebAction.class);

    public DriverWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        // The possible methods here are for location::remoteURL and browser
        //getRunningTest().setContinueTest(false);
    }
}
