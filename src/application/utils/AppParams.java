package application.utils;

import javafx.scene.text.Font;

public class AppParams {
    public static Font getFont() {
        return getFont(12);
    }

    public static String CHROME_DIRECTORY = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe";
    public static String IE_DIRECTORY = "C:\\Program Files\\Internet Explorer\\iexplore.exe\"";
    public static String APP_TITLE = "SpiraLinks Development Engine";
    public static String APP_VERSION = "V0.3";

    public static String MYSQL_CONNECTION = "jdbc:mysql://172.16.10.213/sde?user=spiralinks&password=spiralinks";
    public static String ORACLE_CONNECTION = "jdbc:oracle:thin:@172.16.10.181:1521:FOCALTST";

    public static Font getFont(Integer size) {
        if (size == null) {
            size = 12;
        }

        //Font.font("Verdana", FontWeight.BOLD, size);
        return Font.font("Verdana", size);
    }
}
