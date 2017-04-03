package application.test.action;

import application.test.TestParameter;
import org.apache.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * This action lets keyboard inputs be pressed down, ideal for tab or control+ keys
 */
public class KeyboardWebAction extends WebAction {

    private static Logger log = Logger.getLogger(KeyboardWebAction.class);

    public KeyboardWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter keyToPress = getTestCommand().getParameterByName("press");

        WebElement testElement = specifiedElement();

        log.info("When trying to press tab the element is " + testElement);

        if (testElement != null) {
            if (keyToPress.exists()) {
                if ("tab".equals(keyToPress.getParameterValue())) {
                    testElement.sendKeys(Keys.TAB);
                } else if ("enter".equals(keyToPress.getParameterValue())) {
                    testElement.sendKeys(Keys.ENTER);
                }
            }
        }
    }
}
