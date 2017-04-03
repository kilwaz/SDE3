package sde.application.test.action.helpers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

public class DocumentTracker {
    private Document currentDocument = null;
    private WebDriver driver = null;
    private List<String> iFrameTree = new ArrayList<>(); // Keep track of which iframe we are currently looking in and the tree of where that is positioned

    public DocumentTracker(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * This methods converts the page source returned by Selenium and converts it into a JSoup Document.
     * <p>
     * The reason for this is the overhead that is incurred when interacting with Selenium.  A large amount of
     * actions against Selenium can cause the WebDriver to run close and get overloaded.  Previously it was causing
     * issues with the number of ports on the local machine.  Selenium uses local ports to connect to the WebDriver
     * browser and requesting too much too fast exhausts the current ports that are available.
     * <p>
     * Using JSoup for all lookups and checking the page speeds up the code and drastically and reduces the amount of work
     * we are doing in Selenium.
     */
    public void refreshCurrentDocument() {
        if (driver != null) {
            String pageSource = driver.getPageSource();

            // We check to see if the page source is null, if it is we return a default empty page as to not create null pointers
            if (pageSource != null) {
                currentDocument = Jsoup.parse(pageSource);
                return;
            }
        }
        currentDocument = Jsoup.parse("<html></html>");
    }

    /**
     * This exists for use by actions which extend this class.
     *
     * @return Gets {@link org.jsoup.nodes.Document} that holds the representation for this action.
     */
    public Document getCurrentDocument() {
        return currentDocument;
    }

    public void addIFrameChange(String iFrameName) {
        iFrameTree.add(iFrameName);
    }

    public void setToDefaultFrame() {
        iFrameTree.clear();
    }

    public List<String> getIFrameTree() {
        return iFrameTree;
    }
}
