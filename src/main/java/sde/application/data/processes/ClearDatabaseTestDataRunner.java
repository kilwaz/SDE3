package sde.application.data.processes;

import org.apache.log4j.Logger;
import sde.application.data.model.dao.TestDAO;
import sde.application.node.objects.Test;
import sde.application.utils.SDERunnable;
import sde.application.utils.SDEThread;

import java.util.List;

public class ClearDatabaseTestDataRunner extends SDERunnable {
    private static Logger log = Logger.getLogger(ClearDatabaseTestDataRunner.class);

    public ClearDatabaseTestDataRunner() {

    }

    public static void execute() {
        new SDEThread(new ClearDatabaseTestDataRunner(), "Clean test data", "", true);
    }

    public void threadRun() {
        log.info("Cleaning up old test data");
        TestDAO testDAO = new TestDAO();

        Boolean hasMoreToDelete = true;
        Integer removedCounter = 0;
        while (hasMoreToDelete) {
            log.info("Loading next 10...");
            List<Test> tests = testDAO.getAllTests(10);
            if (tests.size() == 0) {
                hasMoreToDelete = false;
            }
            for (Test test : tests) {
                test.deleteCascade();
                removedCounter++;
                log.info("Removed: " + test.getUuidString());
            }
        }

        log.info("Cleaned up " + removedCounter + " tests");
    }
}
