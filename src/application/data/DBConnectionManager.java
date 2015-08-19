package application.data;

import application.utils.AppParams;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.flywaydb.core.Flyway;

import java.util.ArrayList;
import java.util.List;

public class DBConnectionManager {
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
        node_type enum('ConsoleNode','LogicNode','SwitchNode','TestResultNode','LinuxNode','BashNode','InputNode','TimerNode','TriggerNode','WindowsNode','BatchNode','EmailNode','CopyNode','TestNode','RequestTrackerNode','DataBaseNode','ExportNode','CustomObjectNode','ChartNode'),
        PRIMARY KEY (id),
        FOREIGN KEY (program_id) REFERENCES program(id) ON DELETE CASCADE ON UPDATE CASCADE);

    ** Use this to add another enum type to the node table
    alter table node change node_type node_type enum('ConsoleNode','LogicNode','SwitchNode','TestResultNode','LinuxNode','BashNode','InputNode','TimerNode','TriggerNode','WindowsNode','BatchNode','EmailNode','CopyNode','TestNode','RequestTrackerNode','DataBaseNode','ExportNode','CustomObjectNode','ChartNode');
    alter table node_colour change node_type node_type enum('ConsoleNode','LogicNode','SwitchNode','TestResultNode','LinuxNode','BashNode','InputNode','TimerNode','TriggerNode','WindowsNode','BatchNode','EmailNode','CopyNode','TestNode','RequestTrackerNode','DataBaseNode','ExportNode','CustomObjectNode','ChartNode');
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
        node_type enum('ConsoleNode','LogicNode','SwitchNode','TestResultNode','LinuxNode','BashNode','InputNode','TimerNode','TriggerNode','WindowsNode','BatchNode','EmailNode','CopyNode','TestNode','RequestTrackerNode','DataBaseNode','ExportNode','CustomObjectNode','ChartNode'),
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

    create table serialized(
       id INT NOT NULL AUTO_INCREMENT,
       node_id INT,
       serial_object MEDIUMBLOB,
       serial_reference VARCHAR(1000),
       PRIMARY KEY (id),
       FOREIGN KEY (node_id) REFERENCES node(id) ON DELETE CASCADE ON UPDATE CASCADE);
    */

    private static DBConnectionManager instance;
    private List<DBConnection> DBConnectionList = new ArrayList<>();
    private DBConnection applicationConnection;

    private static Logger log = Logger.getLogger(DBConnectionManager.class);

    public DBConnectionManager() {
        instance = this;
    }

    public void addOracleConnection(DBConnection DBConnection) {
        DBConnectionList.add(DBConnection);
    }

    public void closeConnections() {
        DBConnectionList.forEach(DBConnection::close);
    }

    public Boolean createApplicationConnection() {
        applicationConnection = new DBConnection(AppParams.MYSQL_CONNECTION, AppParams.MYSQL_USERNAME, AppParams.MYSQL_PASSWORD);
        addOracleConnection(applicationConnection);
        if (!applicationConnection.connect()) {
            return false;
        }
        DatabaseConnectionWatcher.getInstance().setConnected(true);

        // Migrate the database
        Flyway flyway = new Flyway();
        flyway.setDataSource(AppParams.MYSQL_CONNECTION, AppParams.MYSQL_USERNAME, AppParams.MYSQL_PASSWORD);

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
        if(instance == null){
            new DBConnectionManager();
        }
        return instance;
    }
} 