package application.test.action;

import application.test.TestParameter;

public class URLAction extends ActionControl {
    // This class is used to go to a specific URL
    public URLAction() {
    }

    public void performAction() {
        TestParameter url = getTestCommand().getParameterByName("url");

        if (url != null) { // Go to the url specified
            getDriver().get(url.getParameterValue());
        }
    }
}





