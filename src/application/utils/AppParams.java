package application.utils;

import javafx.scene.text.Font;

public class AppParams {
    public static Font getFont() {
        return getFont(12);
    }

    public final static String CHROME_DIRECTORY = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe";
    public final static String IE_DIRECTORY = "C:\\Program Files\\Internet Explorer\\iexplore.exe\"";
    public final static String APP_TITLE = "SpiraLinks Development Engine";
    public final static String APP_VERSION = "V0.4.3.5";

    // Database for application - Defaults are displayed here and are overridden by the SDE.xml file
    private static String MYSQL_CONNECTION = "jdbc:mysql://172.16.10.213/sde";
    private static String MYSQL_USERNAME = "spiralinks";
    private static String MYSQL_PASSWORD = "spiralinks";

    // Current user logged into the application
    public static final String CURRENT_USER = "alex";

    public static String getMysqlConnection() {
        return MYSQL_CONNECTION;
    }

    public static String getMysqlUsername() {
        return MYSQL_USERNAME;
    }

    public static String getMysqlPassword() {
        return MYSQL_PASSWORD;
    }

    public static void setMysqlPassword(String mysqlPassword) {
        MYSQL_PASSWORD = mysqlPassword;
    }

    public static void setMysqlUsername(String mysqlUsername) {
        MYSQL_USERNAME = mysqlUsername;
    }

    public static void setMysqlConnection(String mysqlConnection) {
        MYSQL_CONNECTION = mysqlConnection;
    }

    public static Font getFont(Integer size) {
        if (size == null) {
            size = 12;
        }

        //Font.font("Verdana", FontWeight.BOLD, size);
        return Font.font("Verdana", size);
    }
}
