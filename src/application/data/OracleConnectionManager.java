package application.data;

import application.utils.AppParams;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class OracleConnectionManager {
    private Connection connection;
    private static OracleConnectionManager instance;

    public OracleConnectionManager() {
        instance = this;
        try {
            connection = DriverManager.getConnection(AppParams.ORACLE_CONNECTION, "alex_spl_demo_v6", "alex_spl_demo_v6");
            System.out.println("SUCCESS!");
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }
    }

    public Boolean runQuery(String query) {
        Boolean failure = true;
        try {
            Statement statement = connection.createStatement();
            failure = statement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return failure;
    }

    public static OracleConnectionManager getInstance() {
        return instance;
    }
}
