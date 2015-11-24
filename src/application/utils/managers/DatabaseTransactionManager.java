package application.utils.managers;

import application.data.DBConnectionManager;
import application.data.Query;
import application.data.SelectQuery;
import application.data.UpdateQuery;
import application.error.Error;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTransactionManager {
    private static DatabaseTransactionManager databaseTransactionManager;

    private List<Query> pendingQueryList = new ArrayList<>();
    private Boolean inTransaction = false;

    private static Logger log = Logger.getLogger(DatabaseTransactionManager.class);

    public DatabaseTransactionManager() {
        databaseTransactionManager = this;
    }

    public static DatabaseTransactionManager getInstance() {
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

    public synchronized void addUpdate(UpdateQuery updateQuery) {
//        log.info("Update - " + updateQuery.getQuery());
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
        pendingQueryList.add(updateQuery);
    }

    public synchronized void finaliseTransactions() {
        try {
            if (inTransaction) {
                DBConnectionManager.getInstance().getApplicationConnection().getConnection().commit();
                DBConnectionManager.getInstance().getApplicationConnection().getConnection().setAutoCommit(true);
                pendingQueryList.clear();
                inTransaction = false;
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

    public Boolean getInTransaction() {
        return inTransaction;
    }
}
