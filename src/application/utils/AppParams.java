package application.utils;

import javafx.scene.text.Font;

public class AppParams {
    public final static String CHROME_DIRECTORY = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe";
    public final static String IE_DIRECTORY = "C:\\Program Files\\Internet Explorer\\iexplore.exe\"";
    public final static String APP_TITLE = "SpiraLinks Development Engine";
    public final static String APP_VERSION = "V0.4.4.13";
    // Current user logged into the application
    public static final String CURRENT_USER = "alex";
    // Database for application - Defaults are displayed here and are overridden by the SDE.xml file
    private static String REMOTE_DATABASE_CONNECTION = "jdbc:mysql://172.16.10.234/sde";
    private static String LOCAL_DATABASE_NAME = "sde";
    private static String REMOTE_DATABASE_USERNAME = "spiralinks";
    private static String REMOTE_DATABASE_PASSWORD = "spiralinks";
    private static Boolean LOCAL_DATABASE = true;
    private static Boolean CREATE_TEST_DOCUMENT = true;
    private static Boolean RECORD_SCREENSHOTS = true;

    public static Font getFont() {
        return getFont(12);
    }

    public static String getRemoteDatabaseConnection() {
        return REMOTE_DATABASE_CONNECTION;
    }

    public static void setRemoteDatabaseConnection(String databaseConnection) {
        REMOTE_DATABASE_CONNECTION = databaseConnection;
    }

    public static String getRemoteDatabaseUsername() {
        return REMOTE_DATABASE_USERNAME;
    }

    public static void setRemoteDatabaseUsername(String remoteDatabaseUsername) {
        REMOTE_DATABASE_USERNAME = remoteDatabaseUsername;
    }

    public static String getRemoteDatabasePassword() {
        return REMOTE_DATABASE_PASSWORD;
    }

    public static void setRemoteDatabasePassword(String remoteDatabasePassword) {
        REMOTE_DATABASE_PASSWORD = remoteDatabasePassword;
    }

    public static Font getFont(Integer size) {
        if (size == null) {
            size = 12;
        }

        //Font.font("Verdana", FontWeight.BOLD, size);
        return Font.font("Verdana", size);
    }

    public static String getLocalDatabaseName() {
        return LOCAL_DATABASE_NAME;
    }

    public static void setLocalDatabaseName(String databaseName) {
        LOCAL_DATABASE_NAME = databaseName;
    }

    public static Boolean isLocalDatabase() {
        return LOCAL_DATABASE;
    }

    public static void setLocalDatabase(Boolean localDatabase) {
        LOCAL_DATABASE = localDatabase;
    }

    public static Boolean getRecordScreenshots() {
        return RECORD_SCREENSHOTS;
    }

    public static Boolean getCreateTestDocument() {
        return CREATE_TEST_DOCUMENT;
    }
}
