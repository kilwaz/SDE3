package application.utils.managers;

import application.error.Error;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

public class SeleniumGridManager {
    private static final String SELENIUM_HUB_JAR = "selenium-server-standalone-2.53.0.jar";
    private static SeleniumGridManager instance = null;
    private static Logger log = Logger.getLogger(SeleniumGridManager.class);
    private Process hub = null;

    public SeleniumGridManager() {
        instance = this;
        startHubIfRequired();
    }

    public static SeleniumGridManager getInstance() {
        if (instance == null) {
            instance = new SeleniumGridManager();
        }
        return instance;
    }

    public void startHubIfRequired() {
        if (!isHubRunning()) {
            log.info("Trying to start Selenium Hub");
            ProcessBuilder pb = new ProcessBuilder("C:/Program Files/Java/jdk1.8.0_91/bin/java.exe", "-jar", SDEUtils.getResourcePath() + "/WebDrivers/" + SELENIUM_HUB_JAR, "-role", "hub");
            try {
                hub = pb.start();
            } catch (IOException ex) {
                Error.SELENIUM_GRID_HUB_START_FAILED.record().create(ex);
            }
            log.info("Selenium Hub is up and running..");
        }
    }

    public void stopHub() {
        if (hub != null && hub.isAlive()) {
            hub.destroyForcibly();
        }
    }

    public Boolean isHubRunning() {
        return hub != null && hub.isAlive();
    }
}
