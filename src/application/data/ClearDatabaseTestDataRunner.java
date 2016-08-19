package application.data;

import application.data.model.dao.TestCommandDAO;
import application.data.model.dao.TestDAO;
import application.utils.SDERunnable;
import application.utils.SDEThread;

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
