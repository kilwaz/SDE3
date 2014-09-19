package application;

import application.utils.BrowserManager;
import application.utils.DataBank;
import application.utils.SSHConnectionManager;
import application.utils.ThreadManager;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {
    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private Stage mainStage;
    private static final int SPLASH_WIDTH = 940;
    private static final int SPLASH_HEIGHT = 360;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        showSplash(primaryStage);

        loadProgress.setProgress(0.0);
        new MySQLConnectionManager();
        loadProgress.setProgress(0.25);
        new SSHConnectionManager();
        loadProgress.setProgress(0.5);
        new ThreadManager();
        loadProgress.setProgress(0.6);
        new BrowserManager();
        loadProgress.setProgress(0.7);

        DataBank.loadFromDatabase();
        loadProgress.setProgress(0.8);

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
            for (File file : dir.listFiles()) file.delete();
        }

        // System specific
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
        System.setProperty("webdriver.ie.driver", "C:\\Program Files\\Internet Explorer\\iexplore.exe\"");

        loadProgress.setProgress(1.0);
        showMainStage();
    }

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
        splashLayout.setStyle("-fx-padding: 5; -fx-background-color: cornsilk; -fx-border-width:5; -fx-border-color: linear-gradient(to bottom, chocolate, derive(chocolate, 50%));");
        splashLayout.setEffect(new DropShadow());
    }

    private void showMainStage() throws IOException {
        mainStage = new Stage(StageStyle.DECORATED);
        mainStage.setTitle("FX Experience");
        mainStage.setIconified(true);

        //Sets up interface
        Parent root = FXMLLoader.load(getClass().getResource("ApplicationScene.fxml"));
        Scene scene = new Scene(root);
        mainStage.setScene(scene);
        mainStage.setTitle("Spiralinks Development Engine V0.1");
        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                // On Application Close
                SSHConnectionManager.getInstance().closeConnections();
                ThreadManager.getInstance().closeThreads();
                BrowserManager.getInstance().closeBrowsers();

                // Cleans up any class or java files previously compiled.
                String userHome = System.getProperty("user.home");
                File dir = new File(userHome, "/SDE/programs");
                if (dir.exists()) {
                    for (File file : dir.listFiles()) file.delete();
                }
            }
        });

        Controller.getInstance().setScene(scene);

        if (primaryStage.isShowing()) {
            loadProgress.progressProperty().unbind();
            loadProgress.setProgress(1);
            progressText.setText("Finished.");
            mainStage.setIconified(false);
            primaryStage.toFront();
            FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
            fadeSplash.setFromValue(1.0);
            fadeSplash.setToValue(0.0);
            fadeSplash.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    primaryStage.hide();
                    mainStage.show();
                }
            });
            fadeSplash.play();
        }
    }

    private void showSplash(Stage initStage) {
        Scene splashScene = new Scene(splashLayout);
        initStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
