package application;

import application.data.DBConnection;
import application.data.DBConnectionManager;
import application.data.DataBank;
import application.data.DatabaseConnectionWatcher;
import application.gui.Controller;
import application.net.proxy.WebProxyManager;
import application.utils.AppParams;
import application.utils.AppProperties;
import application.utils.SDEUtils;
import application.utils.managers.*;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Main handling class of the application.
 * <p>
 * The job of this class is to prep the application and handle the splash screen.
 * <p>
 * We also handle the shutdown of the application here.
 *
 * @author Alex Brown
 */
public class Main extends Application {
    private static Main instance;

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private Stage mainStage;
    private Stage splashStage;
    private static final int SPLASH_WIDTH = 940;
    private static final int SPLASH_HEIGHT = 360;

    private static Logger log = Logger.getLogger(Main.class);

    /**
     * We start all managers and prep everything we need to get the application running including loading all data from the database.
     * <p>
     * While this is running the splash screen will be shown and a loading bar at the bottom with an updated progress will be available.
     *
     * @param stage Passes in the main stage of the application.
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {
        // This lets all logging be captured and then displayed
        new LogManager();

        instance = this;
        this.splashStage = stage;
        showSplash();

        loadProgress.setProgress(0.0);

        new AppProperties(); // Set the location of where to find the properties xml file
        if (!AppProperties.readXML()) {
            AppProperties.saveToXML();
        }

        new DBConnectionManager();
        new DatabaseConnectionWatcher();  // Creates the database watcher which will let the user know when the database disconnects
        Boolean connectionSuccessful = DBConnectionManager.getInstance().createApplicationConnection();

        loadProgress.setProgress(0.5);

        // Load all managers
        new ThreadManager();
        new SSHConnectionManager();
        new BrowserManager();
        new WebProxyManager();
        new JobManager();
        new TabManager();
        new ErrorManager();
        new NetworkManager();
        new SessionManager();
        new DatabaseObjectManager();
        new DatabaseTransactionManager();

        // Start loading data from the database
        if (connectionSuccessful) {
            DataBank.createCurrentSession();
        }

        //new NetworkBuilder();  // Finds all available IP addresses on the network
        loadProgress.setProgress(0.9);
        //new SDEThread(new WebProxy());
        //new WebRecordListenServer();

        //RemoteDebug remoteDebug = new RemoteDebug("172.16.10.212", "8787");
        //Thread t = new Thread(remoteDebug);
        //t.start();

        //Thread.sleep(200);

//        remoteDebug.setBreakPoint("com.spl.focal.appContext.app.ajaxActions.PerfRatingChangedAction", "fullFieldName", 136);
//        remoteDebug.setBreakPoint("com.spl.focal.appContext.app.ajaxActions.PerfRatingChangedAction", "currencyCd", 145);
//        remoteDebug.setBreakPoint("com.spl.focal.appContext.app.ajaxActions.PerfRatingChangedAction", "roleSeqId", 142);
        //remoteDebug.setBreakPoint("com.spl.focal.appContext.app.ajaxActions.PerfRatingChangedAction", "displayCurrency", 146);

        // Removes any class or java files previously compiled.
        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, "/SDE/programs");
        if (dir.exists()) {
            File[] listedFiles = dir.listFiles();
            if (listedFiles != null) {
                for (File file : listedFiles) {
                    if (file != null && file.exists()) {
                        boolean deleteResult = file.delete();
                    }
                }
            }
        }

        // System specific
        System.setProperty("webdriver.chrome.driver", AppParams.CHROME_DIRECTORY);
        System.setProperty("webdriver.ie.driver", AppParams.IE_DIRECTORY);

        loadProgress.setProgress(1.0);
        showMainStage();
    }

    /**
     * Sets up the initial splash screen before we show it.
     * Called before start()
     */
    @Override
    public void init() {
        URL editorURL = getClass().getResource("/splash.png");
        ImageView splash = new ImageView(new Image(editorURL.toExternalForm()));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
        progressText = new Label("Loading...");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle("-fx-padding: 5; -fx-background-color: cornsilk; -fx-border-width:5;");
        splashLayout.setEffect(new DropShadow());
    }

    /**
     * Shows the main application window after all loading has been completed and the splash screen is closed
     *
     * @throws IOException
     */
    private void showMainStage() throws IOException {
        mainStage = new Stage(StageStyle.DECORATED);
        mainStage.setIconified(true);

        //Sets up interface
        Parent root = FXMLLoader.load(getClass().getResource("/ApplicationScene.fxml"));
        Scene scene = new Scene(root);
        mainStage.setScene(scene);
        mainStage.setTitle(AppParams.APP_TITLE + " " + AppParams.APP_VERSION);
        mainStage.setOnCloseRequest(windowEvent -> {
            // On Application Close we try and clean up all the open connections and running threads
            shutdownApplication();
        });

        URL url = getClass().getResource("/icon.png");
        mainStage.getIcons().add(new Image(url.toExternalForm()));
        mainStage.setMaximized(true);
        Controller.getInstance().setScene(scene);

        if (splashStage.isShowing()) {
            loadProgress.progressProperty().unbind();
            loadProgress.setProgress(1);
            progressText.setText("Finished.");
            mainStage.setIconified(false);
            splashStage.toFront();
            FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
            fadeSplash.setFromValue(1.0);
            fadeSplash.setToValue(0.0);
            fadeSplash.setOnFinished(actionEvent -> {
                splashStage.hide();
                mainStage.show();
            });
            fadeSplash.play();
        }
    }

    /**
     * Shows the splash Spiralinks screen when initially loading into the application.
     */
    private void showSplash() {
        Scene splashScene = new Scene(splashLayout);
        splashStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        splashStage.setScene(splashScene);
        splashStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        splashStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        URL url = getClass().getResource("/icon.png");
        splashStage.getIcons().add(new Image(url.toExternalForm()));
        splashStage.show();
    }

    /**
     * Used to try and gracefully shutdown all opened threads and managers used in the application.
     * Also cleans up temp files used by the java compile process.
     * Closes the main window.
     */
    public void shutdownApplication() {
        // On Application Close we try and clean up all the open connections and running threads
        DatabaseTransactionManager.getInstance().finaliseTransactions();
        SSHConnectionManager.getInstance().closeConnections();
        ThreadManager.getInstance().closeThreads();
        BrowserManager.getInstance().closeBrowsers();
        WebProxyManager.getInstance().closeProxies();
        DBConnectionManager.getInstance().closeConnections();
        JobManager.getInstance().closeAllJobs();

        // Cleans up any class or java files previously compiled.
        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, "/SDE/programs");
        if (dir.exists()) {
            File[] listedFiles = dir.listFiles();
            if (listedFiles != null) {
                for (File file : listedFiles) {
                    boolean deleteResult = file.delete();
                }
            }
        }

        // Deletes any tmp files created from SDEFiles.
        dir = new File(userHome, "/SDE/sdeFiles");
        if (dir.exists()) {
            File[] listedFiles = dir.listFiles();
            if (listedFiles != null) {
                for (File file : listedFiles) {
                    boolean deleteResult = file.delete();
                }
            }
        }

        mainStage.close();
    }

    /**
     * Main method and entry point into the application.
     *
     * @param args Standard args that can be passed into main method, current no args are used.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * @return Returns main stage/window for the application.
     */
    public Stage getMainStage() {
        return mainStage;
    }

    /**
     * @return Returns self.
     */
    public static Main getInstance() {
        return instance;
    }
}
