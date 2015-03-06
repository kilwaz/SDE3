package application.test.action;

import application.test.TestParameter;

public class FrameAction extends ActionControl {
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
    }
}
