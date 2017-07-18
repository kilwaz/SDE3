package sde.application;

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
import sde.application.data.*;
import sde.application.data.model.dao.ProgramDAO;
import sde.application.error.Error;
import sde.application.gui.Controller;
import sde.application.gui.Program;
import sde.application.net.proxy.WebProxyManager;
import sde.application.test.selenium.NodeHelperClient;
import sde.application.test.selenium.NodeHelperSocketServer;
import sde.application.utils.AppParams;
import sde.application.utils.AppProperties;
import sde.application.utils.managers.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * GUI handling class of the application.
 * <p>
 * The job of this class is to prep the application and handle the splash screen.
 * <p>
 * We also handle the shutdown of the application here.
 *
 * @author Alex Brown
 */
public class GUI extends Application {
    private static final int SPLASH_WIDTH = 940;
    private static final int SPLASH_HEIGHT = 360;
    public static Boolean isHeadless = false;
    public static Boolean isSeleniumNodeHelper = false;
    private static Boolean isShowHelp = false;
    private static Boolean useDatabase = true;
    private static GUI instance;
    private static Logger log = Logger.getLogger(GUI.class);
    private static Boolean printJavaProperties = false;
    private static List argsList;
    private static String programNameToRun;

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private Stage mainStage;
    private Stage splashStage;

    /**
     * GUI method and entry point into the application.
     *
     * @param args Standard args that can be passed into main method, current no args are used.
     */
    public static void main(String[] args) {
        argsList = Arrays.asList(args);
        launch(args);
    }

    /**
     * @return Returns self.
     */
    public static GUI getInstance() {
        return instance;
    }

    /**
     * We start all managers and prep everything we need to keystore the application running including loading all data from the database.
     * <p>
     * While this is running the splash screen will be shown and a loading bar at the bottom with an updated progress will be available.
     *
     * @param stage Passes in the main stage of the application.
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {
        new AppProperties(); // Set the location of where to find the properties xml file
        if (!AppProperties.readXML()) {
            AppProperties.saveToXML();
        }

        // This lets all logging be captured and then displayed
        new LogManager();
        log.info(AppParams.APP_TITLE + " " + AppParams.APP_VERSION);
        log.info("Log location = " + LogManager.getInstance().getLogOutputCanonicalPath());

        // Load any command line parameters that have been included
        Integer parameterIndex = 0;
        for (int i = 0; i < argsList.size(); i++) {
            if ("-help".equals(argsList.get(i))) {
                isShowHelp = true;
            }
            if ("-headless".equals(argsList.get(i))) {
                isHeadless = true;
            }
            if ("-show-properties".equals(argsList.get(i))) {
                printJavaProperties = true;
            }
            if ("-run".equals(argsList.get(i))) {
                i++;
                try {
                    programNameToRun = (String) argsList.get(i);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    Error.MISSING_RUN_PROGRAM_NAME.record().create();
                }
            }
            if ("-selenium-node-helper".equals(argsList.get(i))) {
                isSeleniumNodeHelper = true;
                useDatabase = false;
                isHeadless = true;
            }
        }

        if (isShowHelp) {
            log.info("Available options:");
            log.info("-help");
            log.info("-selenium-node-helper");
            log.info("-run");
            log.info("-show-properties");
            log.info("-headless");
            System.exit(0);
            return;
        }

        if (isHeadless) {
            log.info("Running in headless mode");
        }

        if (printJavaProperties) {
            Set<String> systemProperties = System.getProperties().stringPropertyNames();
            log.info("***** START JVM Properties *****");
            for (String name : systemProperties) {
                log.info(name + ": " + System.getProperty(name));
            }
            log.info("***** END JVM Properties *****");
        }

        instance = this;
        if (!isHeadless) {
            this.splashStage = stage;
            showSplash();
        }

        Boolean connectionSuccessful = false;
        if (useDatabase) {
            SQLiteProcess sqLiteProcess = new SQLiteProcess();
            sqLiteProcess.start();

            connectionSuccessful = startDatabase();
        }
        if (!isHeadless) {
            loadProgress.setProgress(0.5);
        }

        startManagers();

        StatisticsManager.getInstance().getTotalStatisticStore().incrementApplicationStart();

        // Start loading data from the database
        if (useDatabase && connectionSuccessful) {
            DataBank.createCurrentSession();
        }

        //new NetworkBuilder();  // Finds all available IP addresses on the network
        if (!isHeadless) {
            loadProgress.setProgress(0.9);
        }

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

        if (!isHeadless) {
            loadProgress.setProgress(1.0);
            showMainStage();
        } else { // If program is in headless mode
            if (programNameToRun != null) { // We need to run this program
                ProgramDAO programDAO = new ProgramDAO();
                Program programToRun = programDAO.getProgramByName(programNameToRun);
                if (programToRun != null) {
                    log.info("Running program " + programToRun.getName());
                    programToRun.loadNodesToFlowController();
                    programToRun.run();
                    log.info("Completed Program");
                } else {
                    log.error("Program with name " + programNameToRun + " was not found");
                }
            } else if (isSeleniumNodeHelper) {
                // Run the selenium node server stuff here
                new NodeHelperSocketServer().useSSL().execute();
            }
        }

        //*** ADD TEST CODE HERE ***//

        //**************************//
    }

    private Boolean startDatabase() {
        DatabaseConnectionWatcher.getInstance(); // Creates the database watcher which will let the user know when the database disconnects
        DataSource applicationDataSource = DataSourceFactory.createApplicationDataSource();
        applicationDataSource.getDbConnection().migrateFlyway();
        return applicationDataSource.isConnected();
    }

    private void startManagers() {
        // Load all managers
        DataSourceManager.getInstance();
        new ThreadManager();
        new WindowManager();
        new SSHConnectionManager();
        new BrowserManager();
        new WebProxyManager();
        JobManager.getInstance();
        new TabManager();
        new ErrorManager();
        new NetworkManager();
        new SessionManager();
        new DatabaseObjectManager();
        new SeleniumHubManager();
        new StatisticsManager();
        new WebSocketListenerManager();
        new WebSocketConnectionManager();
    }

    // Start the application in header mode

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
                // Clean up the splash page as we don't need it anymore
                splashStage = null;
                splashLayout = null;
                loadProgress = null;
                progressText = null;
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
        WindowManager.getInstance().closeAllWindows();
        SSHConnectionManager.getInstance().closeConnections();
        ThreadManager.getInstance().removeInactiveThreads();
        BrowserManager.getInstance().closeBrowsers();
        WebProxyManager.getInstance().closeProxies();
        DataSourceManager.getInstance().closeAllDataSources();
        JobManager.getInstance().stopAllJobs();
        SeleniumHubManager.getInstance().stopHub();
        StatisticsManager.getInstance().saveStatistics();
        WebSocketListenerManager.getInstance().closeAllWebSocketListeners();
        WebSocketConnectionManager.getInstance().closeAllWebSocketConnections();

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
     * @return Returns main stage/window for the application.
     */
    public Stage getMainStage() {
        return mainStage;
    }
}
