package application;

import java.sql.*;

public class MySQLConnectionManager {
    private Connection connect = null;
    private static MySQLConnectionManager instance;
    /*
    create table program(
        id INT NOT NULL AUTO_INCREMENT,
        name VARCHAR(100),
        start_node INT,
        PRIMARY KEY (id));

    create table node(
        id INT NOT NULL AUTO_INCREMENT,
        program_id INT,
        node_type VARCHAR(100),
        PRIMARY KEY (id));

    create table node_details(
        id INT NOT NULL AUTO_INCREMENT,
        node_id INT,
        object_name VARCHAR(100),
        object_class VARCHAR(100),
        object_value BLOB,
        PRIMARY KEY (id));

    create table split(
        id INT NOT NULL AUTO_INCREMENT,
        node_id INT,
        target VARCHAR(100),
        enabled BOOL,
        PRIMARY KEY (id));
    */

    public MySQLConnectionManager() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //connect = DriverManager.getConnection("jdbc:mysql://localhost:13390/sde?user=spiralinks&password=spiralinks");
            connect = DriverManager.getConnection("jdbc:mysql://172.16.10.213/sde?user=spiralinks&password=spiralinks");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        instance = this;
    }

    public Boolean isConnected() {
        if (connect == null) {
            return false;
        }
        try {
            return !connect.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ResultSet runQuery(String query) {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connect.createStatement();
            resultSet = statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    public PreparedStatement getPreparedStatement(String sql) {
        try {
            return connect.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // you need to close all three to make sure
    private void close() {
        //close(resultSet);
        //close(statement);
        close(connect);
    }

    private void close(AutoCloseable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            // don't throw now as it might leave following closables in undefined state
        }
    }

    public static MySQLConnectionManager getInstance() {
        return instance;
    }
} 