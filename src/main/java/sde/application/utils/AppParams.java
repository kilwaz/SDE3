package sde.application.utils;

import javafx.scene.text.Font;
import sde.application.error.Error;
import sde.application.utils.managers.LogManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AppParams {
    // Application information
    public final static String APP_TITLE = "SpiraLinks Automated Testing Engine";
    public final static Integer APP_VERSION_MAJOR = 0;
    public final static Integer APP_VERSION_MINOR = 5;
    public final static Integer APP_VERSION_REVISION = 4;

    // Current user logged into the application
    public static final String CURRENT_USER = "alex";

    // Database information - Defaults are displayed here and can be overridden by the SDE.xml file
    private static String REMOTE_DATABASE_CONNECTION = "jdbc:mysql://172.16.10.217/sde5?autoReconnect=true&useSSL=false";
    private static String SQL_LITE_FILE_NAME = "sqlite-3.19.3.exe";
    private static String LOCAL_DATABASE_NAME = "sde";
    private static String REMOTE_DATABASE_USERNAME = "v8_demo_qa";
    private static String REMOTE_DATABASE_PASSWORD = "C0deFreeze09";
    private static Boolean LOCAL_DATABASE = true;
    private static Boolean AUTO_MANAGE_SELENIUM_HUB = false;

    // Log settings
    private static Boolean IN_APP_LOG_VIEW = false;
    private static String LOG_DIRECTORY = "";

    // Document recording settings
    private static String TEST_DOC_OUTPUT_DIR = "C:\\Users\\alex\\Downloads\\";
    private static Boolean CREATE_TEST_DOCUMENT = true;
    private static Boolean RECORD_SCREENSHOTS = true;

    // Browser settings
    private static Integer BROWSER_DEFAULT_RETRY_COUNT = 3;

    // Database
    private static Integer DATABASE_DELETE_LIMIT = 100;

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

    public static String getAppVersionString() {
        return "V" + APP_VERSION_MAJOR + "." + APP_VERSION_MINOR + "." + APP_VERSION_REVISION;
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

    public static void setRecordScreenshots(Boolean recordScreenshots) {
        RECORD_SCREENSHOTS = recordScreenshots;
    }

    public static Boolean getCreateTestDocument() {
        return CREATE_TEST_DOCUMENT;
    }

    public static void setCreateTestDocument(Boolean createTestDocument) {
        CREATE_TEST_DOCUMENT = createTestDocument;
    }

    public static String getTestDocOutputDir() {
        return TEST_DOC_OUTPUT_DIR;
    }

    public static void setTestDocOutputDir(String testDocOutputDir) {
        TEST_DOC_OUTPUT_DIR = testDocOutputDir;
    }

    public static Boolean getAutoManageSeleniumHub() {
        return AUTO_MANAGE_SELENIUM_HUB;
    }

    public static void setAutoManageSeleniumHub(Boolean autoManageSeleniumHub) {
        AUTO_MANAGE_SELENIUM_HUB = autoManageSeleniumHub;
    }

    public static Boolean getInAppLogView() {
        return IN_APP_LOG_VIEW;
    }

    public static void setInAppLogView(Boolean inAppLogView) {
        IN_APP_LOG_VIEW = inAppLogView;
    }

    public static Integer getDatabaseDeleteLimit() {
        return DATABASE_DELETE_LIMIT;
    }

    public static String getJVMProperty(String key) {
        return System.getProperties().getProperty(key);
    }

    public static String getSqlLiteFileName() {
        return SQL_LITE_FILE_NAME;
    }

    public static String getLogDirectory() {
        return LOG_DIRECTORY;
    }

    public static String getConfiguredLogDirectory() {
        if ("".equals(LOG_DIRECTORY)) {
            LOG_DIRECTORY = LogManager.getInstance().getLogOutputDirectoryCanonicalPath();
        }
        return LOG_DIRECTORY;
    }

    public static void setLogDirectory(String logDirectory) {
        LOG_DIRECTORY = logDirectory;
    }

    public static void setBrowserDefaultRetryCount(Integer browserDefaultRetryCount) {
        BROWSER_DEFAULT_RETRY_COUNT = browserDefaultRetryCount;
    }

    public static Integer getBrowserDefaultRetryCount() {
        return BROWSER_DEFAULT_RETRY_COUNT;
    }

    public static String getMachineName() {
        String hostname = "Unknown Machine";

        try {
            InetAddress address = InetAddress.getLocalHost();
            hostname = address.getHostName();
        } catch (UnknownHostException ex) {
            Error.CANNOT_RESOLVE_HOST_THIS_MACHINE.record().create(ex);
        }

        return hostname;
    }
}
