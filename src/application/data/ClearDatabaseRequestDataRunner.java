package application.data;

import application.data.model.dao.RecordedHeaderDAO;
import application.data.model.dao.RecordedProxyDAO;
import application.data.model.dao.RecordedRequestDAO;
import application.utils.SDERunnable;
import application.utils.SDEThread;

public class ClearDatabaseRequestDataRunner extends SDERunnable {
    public ClearDatabaseRequestDataRunner() {

    }

    public static void execute() {
        new SDEThread(new ClearDatabaseRequestDataRunner(), "Clean request data", "", true);
    }

    public void threadRun() {
        RecordedHeaderDAO recordedHeaderDAO = new RecordedHeaderDAO();
        RecordedRequestDAO recordedRequestDAO = new RecordedRequestDAO();
        RecordedProxyDAO recordedProxyDAO = new RecordedProxyDAO();

        recordedHeaderDAO.deleteAllRecordedHeaders();
        recordedRequestDAO.deleteAllRecordedRequests();
        recordedProxyDAO.deleteAllRecordedProxies();
    }
}
