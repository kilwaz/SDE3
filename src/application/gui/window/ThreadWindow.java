package application.gui.window;

import application.error.Error;
import application.gui.UI;
import application.utils.SDEThread;
import application.utils.managers.ThreadManager;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import org.apache.log4j.Logger;

import java.net.URL;

public class ThreadWindow extends SDEWindow {
    private static Logger log = Logger.getLogger(ThreadWindow.class);

    public ThreadWindow() {
        super();
        init();
    }

    private void init() {
        try {
            StackPane root = new StackPane();

            TableView<SDEThread> threadTableView = new TableView<>();

            TableColumn requestID = new TableColumn("ID");
            requestID.setMinWidth(30);
            requestID.setMaxWidth(50);
            requestID.setCellValueFactory(new PropertyValueFactory<SDEThread, Integer>("id"));

            TableColumn alive = new TableColumn("Alive");
            alive.setCellValueFactory(new PropertyValueFactory<SDEThread, Boolean>("isRunning"));

            TableColumn description = new TableColumn("Description");
            description.setCellValueFactory(new PropertyValueFactory<SDEThread, String>("description"));

            TableColumn object = new TableColumn("Object");
            object.setCellValueFactory(new PropertyValueFactory<SDEThread, String>("string"));

            threadTableView.setItems(ThreadManager.getInstance().getRunningThreads());
            threadTableView.getColumns().addAll(requestID);
            threadTableView.getColumns().addAll(alive);
            threadTableView.getColumns().addAll(description);
            threadTableView.getColumns().addAll(object);

            threadTableView.setMaxHeight(Integer.MAX_VALUE);
            threadTableView.setMaxWidth(Integer.MAX_VALUE);

            UI.setAnchorMargins(threadTableView, 50.0, 0.0, 11.0, 0.0);

            threadTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            root.getChildren().add(threadTableView);

            createScene(root, 900, 800);
            this.setTitle("Current Threads");

            URL urlIcon = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(urlIcon.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            Error.CREATE_THREAD_WINDOW.record().create(ex);
        }
    }
}
