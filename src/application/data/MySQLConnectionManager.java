package application.data;

import application.utils.AppParams;

import java.sql.*;

public class MySQLConnectionManager {
    private Connection connect = null;
    private static MySQLConnectionManager instance;
    /*
    create table program(
        id INT NOT NULL AUTO_INCREMENT,
        name VARCHAR(100),
        start_node INT,
        view_x DOUBLE,
        view_y DOUBLE,
        user_id INT,
        PRIMARY KEY (id),
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE);

    create table node(
        id INT NOT NULL AUTO_INCREMENT,
        program_id INT,
        node_type enum('ConsoleNode','LogicNode','SwitchNode','TestResultNode','LinuxNode','BashNode','InputNode','TimerNode','TriggerNode','WindowsNode','BatchNode','EmailNode','CopyNode','TestNode'),
        PRIMARY KEY (id),
        FOREIGN KEY (program_id) REFERENCES program(id) ON DELETE CASCADE ON UPDATE CASCADE);

    ** Use this to add another enum type to the node table
    alter table node change node_type node_type enum('ConsoleNode','LogicNode','SwitchNode','TestResultNode','LinuxNode','BashNode','InputNode','TimerNode','TriggerNode','WindowsNode','BatchNode','EmailNode','CopyNode','TestNode');
    ** Adds a foreign key to a table
    alter table program add FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE;
    **

    create table node_details(
        id INT NOT NULL AUTO_INCREMENT,
        node_id INT,
        object_name VARCHAR(100),
        object_class enum('java.lang.Double','java.lang.Integer','java.lang.String'),
        object_value BLOB,
        PRIMARY KEY (id),
        FOREIGN KEY (node_id) REFERENCES node(id) ON DELETE CASCADE ON UPDATE CASCADE);

    create table switch(
        id INT NOT NULL AUTO_INCREMENT,
        node_id INT,
        target VARCHAR(100),
        enabled BOOL,
        PRIMARY KEY (id),
        FOREIGN KEY (node_id) REFERENCES node(id) ON DELETE CASCADE ON UPDATE CASCADE);

    create table input(
        id INT NOT NULL AUTO_INCREMENT,
        node_id INT,
        variable_name VARCHAR(100),
        variable_value VARCHAR(100),
        PRIMARY KEY (id),
        FOREIGN KEY (node_id) REFERENCES node(id) ON DELETE CASCADE ON UPDATE CASCADE);

    create table trigger_condition(
        id INT NOT NULL AUTO_INCREMENT,
        node_id INT,
        trigger_watch VARCHAR(100),
        trigger_when VARCHAR(100),
        trigger_then VARCHAR(100),
        PRIMARY KEY (id),
        FOREIGN KEY (node_id) REFERENCES node(id) ON DELETE CASCADE ON UPDATE CASCADE);

    create table node_colour(
        id INT NOT NULL AUTO_INCREMENT,
        node_type enum('ConsoleNode','LogicNode','SwitchNode','TestResultNode','LinuxNode','BashNode','InputNode','TimerNode','TriggerNode','WindowsNode','BatchNode','EmailNode','CopyNode','TestNode'),
        colour_r INT,
        colour_g INT,
        colour_b INT,
        PRIMARY KEY (id));

    create table user(
       id INT NOT NULL AUTO_INCREMENT,
       username VARCHAR(100),
       last_program INT,
       PRIMARY KEY (id),
       FOREIGN KEY (last_program) REFERENCES program(id) ON DELETE CASCADE ON UPDATE CASCADE);

    create table test_result(
       id INT NOT NULL AUTO_INCREMENT,
       PRIMARY KEY (id));

    create table test_step(
       id INT NOT NULL AUTO_INCREMENT,
       test_string VARCHAR(1000),
       expected_equal VARCHAR(1000),
       observed_equal VARCHAR(1000),
       screenshot MEDIUMBLOB,
       successful BOOL,
       test_result INT,
       test_type INT,
       PRIMARY KEY (id),
       FOREIGN KEY (test_result) REFERENCES test_result(id) ON DELETE CASCADE ON UPDATE CASCADE);
    */

    public MySQLConnectionManager() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //connect = DriverManager.getConnection("jdbc:mysql://localhost:13390/sde?user=spiralinks&password=spiralinks");
            connect = DriverManager.getConnection(AppParams.MYSQL_CONNECTION);
        } catch (ClassNotFoundException | SQLException e) {
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
        ResultSet resultSet = null;
        try {
            Statement statement = connect.createStatement();
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