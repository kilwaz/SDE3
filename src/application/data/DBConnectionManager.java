package application.data;

import application.error.Error;
import application.utils.AppParams;
import application.utils.SDEUtils;
import application.utils.managers.DatabaseTransactionManager;
import org.apache.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.dbsupport.FlywaySqlScriptException;

import java.util.ArrayList;
import java.util.List;

public class DBConnectionManager {
    private static DBConnectionManager instance;
    private List<DBConnection> DBConnectionList = new ArrayList<>();
    private DBConnection applicationConnection;

    private static Logger log = Logger.getLogger(DBConnectionManager.class);

    public DBConnectionManager() {
        instance = this;
    }

    public void addConnection(DBConnection DBConnection) {
        DBConnectionList.add(DBConnection);
    }

    public void closeConnections() {
        DBConnectionList.forEach(DBConnection::close);
    }

    public Boolean createApplicationConnection() {
        if (applicationConnection != null) {
            DatabaseTransactionManager.getInstance().finaliseTransactions();
            applicationConnection.close();
        }

        if (AppParams.isLocalDatabase()) {
            applicationConnection = new DBConnection("jdbc:sqlite:" + AppParams.getLocalDatabaseName(), "", "", true);
        } else {
            applicationConnection = new DBConnection(AppParams.getRemoteDatabaseConnection(), AppParams.getRemoteDatabaseUsername(), AppParams.getRemoteDatabasePassword(), true);
        }

        addConnection(applicationConnection);
        if (!applicationConnection.connect()) {
            return false;
        }
        DatabaseConnectionWatcher.getInstance().setConnected(true);

        return true;
    }

    public DBConnection getApplicationConnection() {
        return applicationConnection;
    }

    public static DBConnectionManager getInstance() {
        if (instance == null) {
            new DBConnectionManager();
        }
        return instance;
    }

} 