package application.data;

import application.utils.AppParams;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.flywaydb.core.Flyway;

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
        applicationConnection = new DBConnection(AppParams.getDatabaseConnection(), AppParams.getDatabaseUsername(), AppParams.getDatabasePassword(), true);
        addConnection(applicationConnection);
        if (!applicationConnection.connect()) {
            return false;
        }
        DatabaseConnectionWatcher.getInstance().setConnected(true);

        // Migrate the database
        Flyway flyway = new Flyway();
        flyway.setDataSource(AppParams.getDatabaseConnection(), AppParams.getDatabaseUsername(), AppParams.getDatabasePassword());

        String sqlMigrationPath = "filesystem:" + SDEUtils.getResourcePath() + "/SQL-Migration/";

        flyway.setLocations(sqlMigrationPath);

        String[] flywayLocations = flyway.getLocations();
        for (String aLoc : flywayLocations) {
            log.info("Flyway location for sql = " + aLoc);
        }

        flyway.setBaselineOnMigrate(true);
        flyway.migrate();

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