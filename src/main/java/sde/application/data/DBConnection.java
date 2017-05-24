package sde.application.data;

import org.apache.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.FlywaySqlScriptException;
import sde.application.error.Error;
import sde.application.gui.window.SettingsPickerWindow;
import sde.application.utils.AppParams;
import sde.application.utils.SDEUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection {
    public static final int CONNECTION_STATS = 1;
    public static final int CONNECTION_APP = 2;
    public static final int CONNECTION_OTHER = 3;

    private static Logger log = Logger.getLogger(DBConnection.class);
    private int connectionType = -1;
    private String username = "";
    private String password = "";
    private String connectionString = "";
    private Boolean isApplicationConnection = false;
    private Connection connection = null;

    private static String baseAppSQLPath = "data/baseAppDB.sql";

    public DBConnection(String connectionString, int connectionType) {
        this.connectionType = connectionType;
        this.connectionString = connectionString;
    }

    public DBConnection(String connectionString, String username, String password, int connectionType) {
        this.connectionType = connectionType;
        this.password = password;
        this.username = username;
        this.connectionString = connectionString;
    }

    public DBConnection(String connectionString, String username, String password, Boolean isApplicationConnection, int connectionType) {
        this.connectionType = connectionType;
        this.password = password;
        this.username = username;
        this.connectionString = connectionString;
        this.isApplicationConnection = isApplicationConnection;
    }

    public Boolean connect() {
        try {
            // If the database connection is using sqlite then we need to start the sqlite.exe
            if (connectionString.contains("sqlite")) {
                try {
                    Process sqlite = new ProcessBuilder(SDEUtils.getResourcePath() + "/data/" + AppParams.getSqlLiteFileName(), "sde.db").start();
                    // Here we don't actually do anything with the input from SQLite
                    BufferedReader input = new BufferedReader(new InputStreamReader(sqlite.getInputStream()));
                } catch (IOException ex) {
                    Error.SQLITE_START_EXE.record().create(ex);
                }
            }
            if (username.isEmpty() && password.isEmpty()) {
                log.info("Connecting to " + connectionString);
            } else {
                log.info("Connecting to " + connectionString + " " + username + "/" + password);
            }

            connection = DriverManager.getConnection(connectionString, username, password);
            log.info("Successfully connected to " + connectionString);
            return true;
        } catch (SQLException ex) {
            Error.OPEN_DATABASE_CONNECTION.record().additionalInformation("Connection String:" + connectionString + " Username:" + username + " Password:" + password).create(ex);
            // Opening the settings window as there is a connection issue to the database
            new SettingsPickerWindow();
            return false;
        }
    }

    public PreparedStatement getPreparedStatement(String sql) {
        try {
            return connection.prepareStatement(sql);
        } catch (Exception ex) {
            Error.PREPARE_QUERY.record().create(ex);
        }
        return null;
    }

    public Boolean isConnected() {
        if (connection == null) {
            return false;
        }
        try {
            return !connection.isClosed();
        } catch (SQLException ex) {
            Error.CLOSE_DATABASE_CONNECTION.record().create(ex);
        }
        return false;
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            Error.CLOSE_DATABASE_CONNECTION.record().create(ex);
        }
    }

    public Boolean isApplicationConnection() {
        return isApplicationConnection;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void rebuildDatabase() {
        try {
            getConnection().setAutoCommit(false);

            String path = "";

            if (SDEUtils.isJar()) {
                try {
                    URI uri = SDEUtils.getFile(SDEUtils.getJarURI(), baseAppSQLPath);
                    path = uri.getPath().substring(1); // Substring here to remove the first / from the beginning
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                String resourcesPath = SDEUtils.getResourcePath();
                path = resourcesPath + baseAppSQLPath;
            }

            String content = "";
            String currentQuery = "";
            try {
                byte[] encoded = Files.readAllBytes(Paths.get(path));
                content = new String(encoded, "UTF8");

                String[] sqlQuery = content.split(";");

                for (String query : sqlQuery) {
                    currentQuery = query;
                    getPreparedStatement(query).execute();
                }
            } catch (IOException | SQLException ex) {
                Error.DATABASE_REBUILD_FAILED.record().additionalInformation(currentQuery).create(ex);
            }

            getConnection().commit();
            getConnection().setAutoCommit(true);

            migrateFlyway();
        } catch (SQLException ex) {
            Error.DATABASE_REBUILD_FAILED.record().create(ex);
        }
    }

    public void migrateFlyway() {
        try {
            // Migrate the database
            Flyway flyway = new Flyway();
            log.info("Migrating " + connectionString);
            flyway.setDataSource(connectionString, username, password);

            String path = "";
            if (SDEUtils.isJar()) {
                try {
                    URI uri = SDEUtils.getDirectory(SDEUtils.getJarURI(), "SQL-Migration/");
                    path = "filesystem:" + uri.getPath().substring(1); // Substring here to remove the first / from the beginning
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                path = "filesystem:" + SDEUtils.getResourcePath() + "/SQL-Migration/";
            }

            flyway.setLocations(path);

            String[] flywayLocations = flyway.getLocations();
            for (String aLoc : flywayLocations) {
                log.info("Flyway location for sql = " + aLoc);
            }

            flyway.setBaselineOnMigrate(true);
            flyway.migrate();
        } catch (FlywaySqlScriptException ex) {
            Error.DATABASE_MIGRATE_SQL_FAILED.record().create(ex);
        } catch (FlywayException ex) {
            Error.DATABASE_MIGRATE_FAILED.record().create(ex);
        }
    }

    public int getConnectionType() {
        return connectionType;
    }
}
