package application.test.action;

import application.test.TestParameter;
import org.apache.log4j.Logger;

public class FrameAction extends ActionControl {
    private static Logger log = Logger.getLogger(FrameAction.class);

    // This class is used to close the web driver which in turn closes the window
    public FrameAction() {
    }

    public void performAction() {
        TestParameter frameToSelectById = getTestCommand().getParameterByPath("id");
        TestParameter frameToSelect = getTestCommand().getParameterByPath("select");

        if (frameToSelectById != null) {
            getDriver().switchTo().frame(frameToSelectById.getParameterValue());
        }

        if (frameToSelect != null) {
            getDriver().switchTo().defaultContent();
        }

        refreshCurrentDocument();
    }
}
