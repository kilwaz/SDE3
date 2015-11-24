package application.data;

import application.error.Error;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            connection = DriverManager.getConnection(connectionString, username, password);
            isApplicationConnection = true;
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
}
