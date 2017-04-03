package application.utils;

import application.error.Error;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScreenShotClass {
    public static final int ALL_SCREENS = 0;
    public static final int PRIMARY_SCREEN = 1;
    public static final int APPLICATION_SCREEN = 2;
    public static final int BROWSER_SCREEN = 3;

    private static Logger log = Logger.getLogger(ScreenShotClass.class);

    public static void takeScreenShot(String name) {
        takeScreenShot(PRIMARY_SCREEN, name);
    }

    public static void takeScreenShot(int screen, String name) {
        BufferedImage image = null;
        try {
            String userHome = System.getProperty("user.home");

            Rectangle screenRectangle;

            switch (screen) {
                case 0: // All Screens
                    screenRectangle = new Rectangle();
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    GraphicsDevice[] gs = ge.getScreenDevices();
                    for (GraphicsDevice gd : gs) {
                        GraphicsConfiguration[] gc = gd.getConfigurations();
                        for (GraphicsConfiguration aGc : gc) {
                            screenRectangle = screenRectangle.union(aGc.getBounds());
                        }
                    }
                    break;
                case 1: // Primary Screen
                    screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                    break;
                case 2: // Application Screen
                    screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                    break;
                case 3: // Browser Screen
                    screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                    break;
                default: // Default is Primary Screen
                    screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                    break;
            }

            image = new Robot().createScreenCapture(screenRectangle);
            ImageIO.write(image, "png", new File(userHome, "/SDE/" + name + ".png"));
        } catch (AWTException | IOException ex) {
            Error.TAKE_SCREENSHOT.record().create(ex);
        }
    }

    static public Rectangle getScreenBounds(Window wnd) {
        Rectangle sb;
        Insets si = getScreenInsets(wnd);

        if (wnd == null) {
            sb = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration()
                    .getBounds();
        } else {
            sb = wnd
                    .getGraphicsConfiguration()
                    .getBounds();
        }

        sb.x += si.left;
        sb.y += si.top;
        sb.width -= si.left + si.right;
        sb.height -= si.top + si.bottom;
        return sb;
    }

    static public Insets getScreenInsets(Window wnd) {
        Insets si;

        if (wnd == null) {
            si = Toolkit.getDefaultToolkit().getScreenInsets(GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration());
        } else {
            si = wnd.getToolkit().getScreenInsets(wnd.getGraphicsConfiguration());
        }
        return si;
    }
}
