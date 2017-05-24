package sde.application.data;

import org.apache.log4j.Logger;
import org.quartz.*;
import sde.application.error.Error;
import sde.application.utils.Timer;
import sde.application.utils.managers.DataSourceManager;
import sde.application.utils.managers.JobManager;
import sde.application.utils.timers.TransactionJob;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataSource {
    private DBConnection dbConnection;
    private Boolean isConnected = false;
    private List<Query> pendingQueryList = new ArrayList<>();
    private Boolean inTransaction = false;
    private Timer timeOfLastQuery = new Timer();

    private static Logger log = Logger.getLogger(DataSource.class);

    public DataSource(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
        // Setup job for checking emails
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("dataSource", this);

        JobDetail transactionJob = JobBuilder.newJob(TransactionJob.class).usingJobData(jobDataMap).build();

        SimpleScheduleBuilder transactionSimpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        TriggerBuilder transactionTriggerBuilder = TriggerBuilder.newTrigger();
        transactionSimpleScheduleBuilder.repeatForever().withIntervalInMilliseconds(1000);

        JobManager.getInstance().scheduleJob(transactionJob, transactionTriggerBuilder.withSchedule(transactionSimpleScheduleBuilder).build());
        transactionTriggerBuilder.startNow();
    }

    public synchronized void addSelect(SelectQuery selectQuery) {
        try {
            if (inTransaction) {
                dbConnection.getConnection().commit();
                dbConnection.getConnection().setAutoCommit(true);
                inTransaction = false;
            }
            pendingQueryList.clear();
            pendingQueryList.add(selectQuery);
        } catch (SQLException ex) {
            Error.DATABASE_TRANSACTION.record().create(ex);
        }
    }

    public synchronized void addUpdate(UpdateQuery updateQuery) {
        if (!inTransaction) {
            try {
                inTransaction = true;
                dbConnection.getConnection().setAutoCommit(false);
            } catch (SQLException ex) {
                Error.DATABASE_TRANSACTION.record().create(ex);
            }
        }

        timeOfLastQuery = new Timer();
        pendingQueryList.add(updateQuery);
    }

    public Boolean connect() {
        if (dbConnection != null) {
            isConnected = dbConnection.connect();
        }
        return isConnected;
    }

    public synchronized void finaliseTransactions() {
        try {
            if (inTransaction) {
                if (dbConnection != null && dbConnection.getConnection() != null) {
                    dbConnection.getConnection().commit();
                    dbConnection.getConnection().setAutoCommit(true);
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
                finaliseTransactions();
            }
        }
    }

    public int getDataSourceType() {
        return dbConnection.getConnectionType();
    }

    public DBConnection getDbConnection() {
        return dbConnection;
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;
    }

    public Boolean isConnected() {
        return isConnected;
    }

    public void closeDownGracefully() {
        finaliseTransactions();
        dbConnection.close();
        DataSourceManager.getInstance().removeDataSource(this);
    }
}
