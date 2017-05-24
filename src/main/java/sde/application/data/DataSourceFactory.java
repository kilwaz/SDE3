package sde.application.data;

import org.apache.log4j.Logger;
import sde.application.utils.AppParams;
import sde.application.utils.managers.DataSourceManager;

import java.io.File;

public class DataSourceFactory {
    private static Logger log = Logger.getLogger(DataSourceFactory.class);

    public static DataSource createStatisticsDataSource() {
        DataSource applicationDataSource;
        DBConnection applicationConnection;

        File databaseFile = new File(AppParams.getLocalDatabaseName());
        log.info("Database location: " + databaseFile.getAbsoluteFile());
        Boolean rebuildNewDatabase = false;
        if (!databaseFile.exists()) {
            rebuildNewDatabase = true;
        }

        if (AppParams.isLocalDatabase()) {
            applicationConnection = new DBConnection("jdbc:sqlite:" + AppParams.getLocalDatabaseName(), "", "", true, DBConnection.CONNECTION_APP);
        } else {
            applicationConnection = new DBConnection(AppParams.getRemoteDatabaseConnection(), AppParams.getRemoteDatabaseUsername(), AppParams.getRemoteDatabasePassword(), true, DBConnection.CONNECTION_APP);
        }

        applicationDataSource = new DataSource(applicationConnection);
        applicationDataSource.connect();

        if (rebuildNewDatabase && AppParams.isLocalDatabase()) { // We only auto rebuild local databases at the moment
            applicationConnection.rebuildDatabase();
        }

        // Add this data source to the manager
        DataSourceManager.getInstance().addDataSource(applicationDataSource);
        // Update the connected readout on the UI
        DatabaseConnectionWatcher.getInstance().setConnected(applicationDataSource.getDbConnection().isConnected());

        return applicationDataSource;
    }

    public static DataSource createApplicationDataSource() {
        DataSource applicationDataSource;
        DBConnection applicationConnection;

        File databaseFile = new File(AppParams.getLocalDatabaseName());
        log.info("Database location: " + databaseFile.getAbsoluteFile());
        Boolean rebuildNewDatabase = false;
        if (!databaseFile.exists()) {
            rebuildNewDatabase = true;
        }

        if (AppParams.isLocalDatabase()) {
            applicationConnection = new DBConnection("jdbc:sqlite:" + AppParams.getLocalDatabaseName(), "", "", true, DBConnection.CONNECTION_APP);
        } else {
            applicationConnection = new DBConnection(AppParams.getRemoteDatabaseConnection(), AppParams.getRemoteDatabaseUsername(), AppParams.getRemoteDatabasePassword(), true, DBConnection.CONNECTION_APP);
        }

        applicationDataSource = new DataSource(applicationConnection);
        applicationDataSource.connect();

        if (rebuildNewDatabase && AppParams.isLocalDatabase()) { // We only auto rebuild local databases at the moment
            applicationConnection.rebuildDatabase();
        }

        // Add this data source to the manager
        DataSourceManager.getInstance().addDataSource(applicationDataSource);
        // Update the connected readout on the UI
        DatabaseConnectionWatcher.getInstance().setConnected(applicationDataSource.getDbConnection().isConnected());

        return applicationDataSource;
    }
}
