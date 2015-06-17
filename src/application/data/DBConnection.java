package application.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection {
    private String username = "";
    private String password = "";
    private String connectionString = "";

    private Connection connection = null;

    public DBConnection(String connectionString, String username, String password) {
        this.password = password;
        this.username = username;
        this.connectionString = connectionString;
    }

    public Boolean connect() {
        try {
            connection = DriverManager.getConnection(connectionString, username, password);
            return true;
        } catch (SQLException e) {
            System.out.println("Trouble connecting to the database with details - Connection String:" + connectionString + " Username:" + username + " Password:" + password);
            return false;
        }
    }

    public PreparedStatement getPreparedStatement(String sql) {
        try {
            return connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean isConnected() {
        if (connection == null) {
            return false;
        }
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
