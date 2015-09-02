package application.test.action;

import org.apache.log4j.Logger;

public class EndAction extends ActionControl {

    private static Logger log = Logger.getLogger(EndAction.class);

    public EndAction() {
    }

    public void performAction() {
        log.info("Stopping test");
        getParentTestNode().setContinueTest(false);
    }
}
