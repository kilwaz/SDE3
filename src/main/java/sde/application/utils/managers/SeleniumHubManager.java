package sde.application.utils.managers;

import sde.application.error.Error;
import sde.application.utils.AppParams;
import sde.application.utils.ProcessLogger;
import sde.application.utils.SDEThread;
import sde.application.utils.SDEUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

public class SeleniumHubManager {
    private static final String SELENIUM_HUB_JAR = "selenium-server-standalone-3.3.0.jar";
    private static SeleniumHubManager instance = null;
    private static Logger log = Logger.getLogger(SeleniumHubManager.class);
    private Process hub = null;

    public SeleniumHubManager() {
        instance = this;
        startHubIfRequired();
    }

    public static SeleniumHubManager getInstance() {
        if (instance == null) {
            instance = new SeleniumHubManager();
        }
        return instance;
    }

    public void startHubIfRequired() {
        if (!isHubRunning() && AppParams.getAutoManageSeleniumHub()) {
            log.info("Trying to start Selenium Hub");
            ProcessBuilder pb = new ProcessBuilder("C:/Program Files/Java/jdk1.8.0_91/bin/java.exe", "-jar", SDEUtils.getResourcePath() + "/WebDrivers/" + SELENIUM_HUB_JAR, "-role", "hub");
            try {
                pb.redirectErrorStream(true);
                hub = pb.start();
                ProcessLogger errorLogger = new ProcessLogger(hub.getErrorStream(), "Selenium Hub Error");
                ProcessLogger inputLogger = new ProcessLogger(hub.getInputStream(), "Selenium Hub Info");

                SDEThread errorThread = new SDEThread(errorLogger, "Selenium Hub Error Process Log", null, true);
                SDEThread infoThread = new SDEThread(inputLogger, "Selenium Hub Input Process Log", null, true);
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
