package application.data;

import application.utils.AppParams;
import application.utils.SDEUtils;
import application.utils.managers.DatabaseTransactionManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DBConnectionManager {
    private static DBConnectionManager instance;
    private static Logger log = Logger.getLogger(DBConnectionManager.class);
    private List<DBConnection> DBConnectionList = new ArrayList<>();
    private DBConnection applicationConnection;

    public DBConnectionManager() {
        instance = this;
    }

    public static DBConnectionManager getInstance() {
        if (instance == null) {
            new DBConnectionManager();
        }
        return instance;
    }

    public void addConnection(DBConnection DBConnection) {
        DBConnectionList.add(DBConnection);
    }


    public void closeConnections() {
        DBConnectionList.forEach(DBConnection::close);
    }

    public Boolean createApplicationConnection() {
        File databaseFile = new File(AppParams.getLocalDatabaseName());
        log.info("Database location: " + databaseFile.getAbsoluteFile());
        Boolean rebuildNewDatabase = false;
        if (!databaseFile.exists()) {
            rebuildNewDatabase = true;
        }
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

        if (rebuildNewDatabase && AppParams.isLocalDatabase()) { // We only auto rebuild local databases at the moment
            applicationConnection.rebuildDatabase();
        }

        return true;
    }

    public DBConnection getApplicationConnection() {
        return applicationConnection;
    }

} 