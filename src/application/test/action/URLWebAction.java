package application.test.action;

import application.test.TestParameter;

/**
 * This action loads a URL that is given to it.
 */
public class URLWebAction extends WebAction {
    // This class is used to go to a specific URL
    public URLWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter url = getTestCommand().getParameterByName("url");
        TestParameter redirectFromURL = getTestCommand().getParameterByName("redirectFrom");
        TestParameter redirectToURL = getTestCommand().getParameterByName("redirectTo");

        if (redirectFromURL.exists() && redirectToURL.exists()) {
            getHttpProxyServer().getWebProxyRequestManager().addRedirectURL(redirectFromURL.getParameterValue(), redirectToURL.getParameterValue());
        }

        if (url.exists()) { // Go to the url specified
            getDriver().get(url.getParameterValue());
        }
    }
}





