package application.data;

import application.error.Error;
import application.utils.SDEUtils;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.apache.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.FlywaySqlScriptException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection {
    private String username = "";
    private String password = "";
    private String connectionString = "";
    private Boolean isApplicationConnection = false;

    private Connection connection = null;

    private static Logger log = Logger.getLogger(DBConnection.class);

    public DBConnection(String connectionString) {
        this.connectionString = connectionString;
    }

    public DBConnection(String connectionString, String username, String password) {
        this.password = password;
        this.username = username;
        this.connectionString = connectionString;
    }

    public DBConnection(String connectionString, String username, String password, Boolean isApplicationConnection) {
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
                    Process sqlite = new ProcessBuilder(SDEUtils.getResourcePath() + "/data/sqlite3.exe", "sde.db").start();
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
            return true;
        } catch (SQLException ex) {
            Error.OPEN_DATABASE_CONNECTION.record().additionalInformation("Connection String:" + connectionString + " Username:" + username + " Password:" + password).create(ex);
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

            String resourcesPath = SDEUtils.getResourcePath();
            String bashEditorPath = resourcesPath + "/data/blankdb.sql";

            String content = "";
            String currentQuery = "";
            try {
                byte[] encoded = Files.readAllBytes(Paths.get(bashEditorPath));
                content = new String(encoded, "UTF8");

                String[] sqlQuery = content.split(";");

                for (String query : sqlQuery) {
                    currentQuery = query;
                    getPreparedStatement(query).execute();
                }
            } catch (IOException | MySQLSyntaxErrorException ex) {
                application.error.Error.DATABASE_REBUILD_FAILED.record().additionalInformation(currentQuery).create(ex);
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

            String sqlMigrationPath = "filesystem:" + SDEUtils.getResourcePath() + "/SQL-Migration/";

            flyway.setLocations(sqlMigrationPath);

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
}
