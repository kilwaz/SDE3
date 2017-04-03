package sde.application.test.action;


import org.apache.log4j.Logger;

/**
 * This action is used to manually refresh the current document in case it has changed for some reason
 */

public class RefreshWebAction extends WebAction {
    private static Logger log = Logger.getLogger(RefreshWebAction.class);

    public RefreshWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        getDocumentTracker().refreshCurrentDocument();
    }
}





