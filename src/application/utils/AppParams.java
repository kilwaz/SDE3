package application.utils;

import javafx.scene.text.Font;

public class AppParams {
    public static Font getFont() {
        return getFont(12);
    }

    public final static String CHROME_DIRECTORY = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe";
    public final static String IE_DIRECTORY = "C:\\Program Files\\Internet Explorer\\iexplore.exe\"";
    public final static String APP_TITLE = "SpiraLinks Development Engine";
    public final static String APP_VERSION = "V0.4.4.1";

    // Database for application - Defaults are displayed here and are overridden by the SDE.xml file
    private static String DATABASE_CONNECTION = "jdbc:mysql://172.16.10.234/sde";
    private static String DATABASE_USERNAME = "spiralinks";
    private static String DATABASE_PASSWORD = "spiralinks";

    // Current user logged into the application
    public static final String CURRENT_USER = "alex";

    public static String getDatabaseConnection() {
        return DATABASE_CONNECTION;
    }

    public static String getDatabaseUsername() {
        return DATABASE_USERNAME;
    }

    public static String getDatabasePassword() {
        return DATABASE_PASSWORD;
    }

    public static void setDatabasePassword(String databasePassword) {
        DATABASE_PASSWORD = databasePassword;
    }

    public static void setDatabaseUsername(String databaseUsername) {
        DATABASE_USERNAME = databaseUsername;
    }

    public static void setDatabaseConnection(String databaseConnection) {
        DATABASE_CONNECTION = databaseConnection;
    }

    public static Font getFont(Integer size) {
        if (size == null) {
            size = 12;
        }

        //Font.font("Verdana", FontWeight.BOLD, size);
        return Font.font("Verdana", size);
    }
}
