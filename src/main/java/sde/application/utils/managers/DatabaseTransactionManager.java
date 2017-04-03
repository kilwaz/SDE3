package sde.application.utils.managers;

import sde.application.data.DBConnectionManager;
import sde.application.data.Query;
import sde.application.data.SelectQuery;
import sde.application.data.UpdateQuery;
import sde.application.error.Error;
import sde.application.utils.AppParams;
import sde.application.utils.Timer;
import sde.application.utils.timers.TransactionJob;
import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTransactionManager {
    private static DatabaseTransactionManager databaseTransactionManager;
    private static Logger log = Logger.getLogger(DatabaseTransactionManager.class);
    private List<Query> pendingQueryList = new ArrayList<>();
    private Boolean inTransaction = false;
    private Timer timeOfLastQuery = new Timer();

    public DatabaseTransactionManager() {
        databaseTransactionManager = this;

        JobDetail transactionJob = JobBuilder.newJob(TransactionJob.class).build();
        //JobDetail dataDataJob = JobBuilder.newJob(DeleteDataJob.class).build();

        SimpleScheduleBuilder transactionSimpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        //SimpleScheduleBuilder deleteDataSimpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        TriggerBuilder transactionTriggerBuilder = TriggerBuilder.newTrigger();
        //TriggerBuilder deleteDataTriggerBuilder = TriggerBuilder.newTrigger();

        transactionSimpleScheduleBuilder.repeatForever().withIntervalInMilliseconds(1000);
        //deleteDataSimpleScheduleBuilder.repeatForever().withIntervalInMilliseconds(5000);

        JobManager.getInstance().scheduleJob(transactionJob, transactionTriggerBuilder.withSchedule(transactionSimpleScheduleBuilder).build());
        //JobManager.getInstance().scheduleJob(dataDataJob, deleteDataTriggerBuilder.withSchedule(deleteDataSimpleScheduleBuilder).build());
        transactionTriggerBuilder.startNow();
        //deleteDataTriggerBuilder.startNow();
    }

    public static synchronized DatabaseTransactionManager getInstance() {
        if (databaseTransactionManager == null) {
            databaseTransactionManager = new DatabaseTransactionManager();
        }
        return databaseTransactionManager;
    }

    public synchronized void addSelect(SelectQuery selectQuery) {
//        log.info("Select - " + selectQuery.getQuery());
//        logQueryParams(selectQuery);
        try {
            if (inTransaction) {
                DBConnectionManager.getInstance().getApplicationConnection().getConnection().commit();
                DBConnectionManager.getInstance().getApplicationConnection().getConnection().setAutoCommit(true);
//                log.info("Committed and transaction ended");
                inTransaction = false;
            }
            pendingQueryList.clear();
            pendingQueryList.add(selectQuery);
        } catch (SQLException ex) {
            Error.DATABASE_TRANSACTION.record().create(ex);
        }
    }

    public synchronized void scheduledDelete() {
        new UpdateQuery("delete from http_headers where forDelete = 1 limit " + AppParams.getDatabaseDeleteLimit()).execute();
        new UpdateQuery("delete from http_proxies where forDelete = 1 limit " + AppParams.getDatabaseDeleteLimit()).execute();
        new UpdateQuery("delete from recorded_requests where forDelete = 1 limit " + AppParams.getDatabaseDeleteLimit()).execute();
        new UpdateQuery("delete from test_command where forDelete = 1 limit " + AppParams.getDatabaseDeleteLimit()).execute();
        new UpdateQuery("delete from test where forDelete = 1 limit " + AppParams.getDatabaseDeleteLimit()).execute();
    }

    public synchronized void addUpdate(UpdateQuery updateQuery) {
//       log.info("Update - " + updateQuery.getQuery());
//        logQueryParams(updateQuery);
        if (!inTransaction) {
            try {
                inTransaction = true;
                DBConnectionManager.getInstance().getApplicationConnection().getConnection().setAutoCommit(false);
//                log.info("Transaction started");
            } catch (SQLException ex) {
                Error.DATABASE_TRANSACTION.record().create(ex);
            }
        }

        timeOfLastQuery = new Timer();
        pendingQueryList.add(updateQuery);
    }

    public synchronized void finaliseTransactions() {
        try {
            if (inTransaction) {
                if (DBConnectionManager.getInstance().getApplicationConnection() != null && DBConnectionManager.getInstance().getApplicationConnection().getConnection() != null) {
                    DBConnectionManager.getInstance().getApplicationConnection().getConnection().commit();
                    DBConnectionManager.getInstance().getApplicationConnection().getConnection().setAutoCommit(true);
                    pendingQueryList.clear();
                    inTransaction = false;
                }
            }
        } catch (SQLException ex) {
            Error.DATABASE_TRANSACTION.record().create(ex);
        }
    }

    private void logQueryParams(Query query) {
        List<Object> params = query.getParameters();
        for (Object o : params) {
            log.info("   " + o.toString());
        }
    }

    public void checkIfNeedToFinalise() {
        if (inTransaction) {
            if (pendingQueryList.size() > 0 && timeOfLastQuery.getTimeSince() > 1000) {
                //log.info("Committing " + pendingQueryList.size() + " query transactions after 1000ms");
                finaliseTransactions();
            }
        }
    }
}
