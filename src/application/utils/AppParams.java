package application.utils;

import javafx.scene.text.Font;

public class AppParams {
    public static Font getFont() {
        return getFont(12);
    }

    public static String CHROME_DIRECTORY = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe";
    public static String IE_DIRECTORY = "C:\\Program Files\\Internet Explorer\\iexplore.exe\"";
    public static String APP_TITLE = "SpiraLinks Development Engine";
    public static String APP_VERSION = "V0.4.2.2";

    // Database for application - Defaults are displayed here and are overridden by the SDE.xml file
    public static String MYSQL_CONNECTION = "jdbc:mysql://172.16.10.213/sde";
    public static String MYSQL_USERNAME = "spiralinks";
    public static String MYSQL_PASSWORD = "spiralinks";

    // Current user logged into the application
    public static String CURRENT_USER = "alex";

    public static Font getFont(Integer size) {
        if (size == null) {
            size = 12;
        }

        //Font.font("Verdana", FontWeight.BOLD, size);
        return Font.font("Verdana", size);
    }
}
