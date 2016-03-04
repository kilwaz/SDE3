package application.test.action;

import application.test.TestParameter;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;

import java.util.Set;

/**
 * This action loads a URL that is given to it.
 */
public class URLWebAction extends WebAction {

    private static Logger log = Logger.getLogger(URLWebAction.class);

    // This class is used to go to a specific URL
    public URLWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter url = getTestCommand().getParameterByName("url");
        TestParameter newWindow = getTestCommand().getParameterByName("newWindow");
        TestParameter redirectFromURL = getTestCommand().getParameterByName("redirectFrom");
        TestParameter redirectToURL = getTestCommand().getParameterByName("redirectTo");

        if (redirectFromURL.exists() && redirectToURL.exists()) {
            getHttpProxyServer().getWebProxyRequestManager().addRedirectURL(redirectFromURL.getParameterValue(), redirectToURL.getParameterValue());
        }

        if (url.exists()) { // Go to the url specified
            if (newWindow.exists()) { // Open in a new window if that is specified
                // Get current windows
                Set<String> currentHandles = getDriver().getWindowHandles();

                // Open a new window with the wanted url
                String script = "var d=document,a=d.createElement('a');a.target='_blank';a.href='" + url.getParameterValue() + "';a.innerHTML='.';d.body.appendChild(a);a.click();";
                ((JavascriptExecutor) getDriver()).executeScript(script);

                // Get windows after the link has opened
                Set<String> afterHandles = getDriver().getWindowHandles();

                // Compare it to the new handles and try to switch to the new window
                for (String handle : afterHandles) {
                    if (!currentHandles.contains(handle)) {
                        getDriver().switchTo().window(handle);
                        break;
                    }
                }
            } else {
                getDriver().get(url.getParameterValue());
            }
        }
    }
}