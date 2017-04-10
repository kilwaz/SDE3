package sde.application.gui.window.setting;

import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import sde.application.gui.Controller;
import sde.application.utils.AppParams;
import sde.application.utils.AppProperties;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;

public class LogSettingsWindow extends SettingsPage {
    private static Logger log = Logger.getLogger(LogSettingsWindow.class);

    public LogSettingsWindow() {
        super();
    }

    public void setupChildren() {

    }

    public AnchorPane getInterface() {
        AnchorPane anchorPane = new AnchorPane();

        VBox rows = new VBox(10);
        rows.setPadding(new Insets(10, 10, 10, 10));

        // Current log file
        HBox currentLogFileRow = new HBox(5);
        currentLogFileRow.setAlignment(Pos.CENTER_LEFT);
        Label currentLogFileLabel = new Label("Current log file:");
        Label currentLogFileValue = new Label(sde.application.utils.managers.LogManager.getInstance().getLogOutputCanonicalPath());

        Controller.getInstance().makeSelectable(currentLogFileValue);

        currentLogFileRow.getChildren().add(currentLogFileLabel);
        currentLogFileRow.getChildren().add(currentLogFileValue);

        // Log file directory
        HBox logFileDirectoryRow = new HBox(5);
        logFileDirectoryRow.setAlignment(Pos.CENTER_LEFT);
        Label logFileDirectoryLabel = new Label("Log Directory:");
        TextField logFileDirectoryValue = new TextField();
        logFileDirectoryValue.setText(AppParams.getConfiguredLogDirectory());
        logFileDirectoryValue.setPrefWidth(400.0);
        logFileDirectoryValue.setOnKeyReleased(event -> {
            AppParams.setLogDirectory(((TextField) event.getSource()).getText());
            AppProperties.saveToXML();
        });

        logFileDirectoryRow.getChildren().add(logFileDirectoryLabel);
        logFileDirectoryRow.getChildren().add(logFileDirectoryValue);

        // In app log viewer
        HBox inAppLogViewRow = new HBox(5);
        inAppLogViewRow.setAlignment(Pos.CENTER_LEFT);
        Label inAppLogViewLabel = new Label("In App Log view?");
        CheckBox inAppLogViewCheckBox = new CheckBox();
        inAppLogViewCheckBox.setSelected(AppParams.getInAppLogView());
        inAppLogViewCheckBox.setOnMouseClicked(event -> {
            AppParams.setInAppLogView(((CheckBox) event.getSource()).isSelected());
            AppProperties.saveToXML();
        });

        inAppLogViewRow.getChildren().add(inAppLogViewLabel);
        inAppLogViewRow.getChildren().add(inAppLogViewCheckBox);

        // Add each row to the main VBox
        rows.getChildren().add(currentLogFileRow);
        rows.getChildren().add(logFileDirectoryRow);
        rows.getChildren().add(inAppLogViewRow);

        anchorPane.getChildren().add(rows);

        return anchorPane;
    }

    public String getName() {
        return "Log Settings";
    }
}

