package sde.application.data;

import sde.application.data.model.dao.TestCommandDAO;
import sde.application.data.model.dao.TestDAO;
import sde.application.utils.SDERunnable;
import sde.application.utils.SDEThread;

public class ClearDatabaseTestDataRunner extends SDERunnable {
    public ClearDatabaseTestDataRunner() {

    }

    public static void execute() {
        new SDEThread(new ClearDatabaseTestDataRunner(), "Clean test data", "", true);
    }

    public void threadRun() {
        TestDAO testDAO = new TestDAO();
        TestCommandDAO testCommandDAO = new TestCommandDAO();

        testDAO.deleteAllTests();
        testCommandDAO.deleteAllTestCommands();
    }
}
