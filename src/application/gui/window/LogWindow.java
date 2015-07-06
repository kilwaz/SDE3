package application.gui.window;

import application.log.LogClass;
import application.log.LogManager;
import application.log.LogMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.*;

public class LogWindow extends Stage {
    private TextArea loggingTextArea;
    private ListView<LogClass> listView;

    private static Logger log = Logger.getLogger(LogWindow.class);

    private static List<LogWindow> logWindows = new ArrayList<>();

    public LogWindow() {
        init();
    }

    private void init() {
        try {
            logWindows.add(this);

            loggingTextArea = new TextArea();
            loggingTextArea.setEditable(false);
            loggingTextArea.setStyle("-fx-font-family: 'Courier New', Courier, mono");

            HBox hbox = new HBox(5);

            listView = new ListView(LogManager.getInstance().getLogClasses());
            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listView.setPrefWidth(250);
            listView.setMaxWidth(250);

            listView.setOnMouseClicked(event -> {
                updateTextArea();
            });

            StackPane root = new StackPane();

            // List view sits on the left side of the window, text area on the right
            hbox.getChildren().add(listView);
            hbox.getChildren().add(loggingTextArea);
            // This is needed in order to get the text area to fill the remaining space
            hbox.setHgrow(loggingTextArea, Priority.ALWAYS);

            root.getChildren().add(hbox);

            this.setScene(new Scene(root, 900, 800));
            this.setTitle("Log");

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
            this.setOnCloseRequest(event -> {
                logWindows.remove(this);
            });
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    private void updateTextArea() {
        loggingTextArea.clear();
        ObservableList<LogClass> selectedItems = listView.getSelectionModel().getSelectedItems();

        HashMap<Long, List<LogMessage>> messagesToBeAdded = new HashMap<>();

        for (LogClass logClass : selectedItems) {
            for (LogMessage logMessage : logClass.getLogMessages()) {
                if (messagesToBeAdded.containsKey(logMessage.getTimeStamp())) {
                    List<LogMessage> logList = messagesToBeAdded.get(logMessage.getTimeStamp());
                    logList.add(logMessage);
                } else {
                    List<LogMessage> newList = new ArrayList<>();
                    newList.add(logMessage);
                    messagesToBeAdded.put(logMessage.getTimeStamp(), newList);
                }
            }
        }

        SortedSet<Long> keys = new TreeSet<>(messagesToBeAdded.keySet());
        for (Long key : keys) {
            List<LogMessage> logList = messagesToBeAdded.get(key);
            for (LogMessage logMessage : logList) {
                addLogMessage(logMessage);
            }
        }
    }

    public void addLogMessage(LogMessage logMessage) {
        try {
            Platform.runLater(() -> {
                try {
                    if (loggingTextArea != null) {
                        if (loggingTextArea.getText().length() == 0) {
                            loggingTextArea.setText(logMessage.getLogMessage());
                        } else {
                            loggingTextArea.selectEnd();
                            loggingTextArea.insertText(loggingTextArea.getText().length(), logMessage.getLogMessage());
                        }
                    }
                } catch (final Throwable t) {
                    // We send this to System Out as it means the logger isn't working correctly
                    System.out.println("Unable to append log to text area: " + t.getMessage());
                }
            });
        } catch (final IllegalStateException ex) {
            log.error(ex);
        }
    }

    public void forceRefresh() {
        Platform.runLater(() -> {
            ObservableList<LogClass> items = listView.getItems();
            ObservableList<LogClass> selectedItems = FXCollections.observableArrayList();
            selectedItems.addAll(listView.getSelectionModel().getSelectedItems());

            listView.setItems(null);
            listView.setItems(items);

            for (LogClass logClass : selectedItems) {
                listView.getSelectionModel().select(logClass);
                listView.getFocusModel().focus(selectedItems.indexOf(logClass));
            }

            updateTextArea();
        });
    }

    public static List<LogWindow> getLogWindows() {
        return logWindows;
    }
}
