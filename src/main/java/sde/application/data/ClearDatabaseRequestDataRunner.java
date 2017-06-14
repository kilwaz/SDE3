package sde.application.data;

import org.apache.log4j.Logger;
import sde.application.data.model.dao.RecordedProxyDAO;
import sde.application.net.proxy.RecordedProxy;
import sde.application.utils.SDERunnable;
import sde.application.utils.SDEThread;

import java.util.List;

public class ClearDatabaseRequestDataRunner extends SDERunnable {
    private static Logger log = Logger.getLogger(ClearDatabaseRequestDataRunner.class);

    public ClearDatabaseRequestDataRunner() {

    }

    public static void execute() {
        new SDEThread(new ClearDatabaseRequestDataRunner(), "Clean request data", "", true);
    }

    public void threadRun() {
        log.info("Cleaning up old proxies");
        RecordedProxyDAO recordedProxyDAO = new RecordedProxyDAO();

        Boolean hasMoreToDelete = true;
        Integer removedCounter = 0;
        while (hasMoreToDelete) {
            log.info("Loading next 10...");
            List<RecordedProxy> recordedProxies = recordedProxyDAO.getAllProxies(10);
            if (recordedProxies.size() == 0) {
                hasMoreToDelete = false;
            }
            for (RecordedProxy recordedProxy : recordedProxies) {
                recordedProxy.deleteCascade();
                removedCounter++;
                log.info("Removed: " + recordedProxy.getUuidString());
            }
        }

        log.info("Cleaned up " + removedCounter + " proxies");
    }
}
