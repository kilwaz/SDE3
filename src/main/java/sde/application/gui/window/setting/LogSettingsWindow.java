package sde.application.gui.window.setting;

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

        VBox rows = new VBox(40);
        rows.setPadding(new Insets(10, 10, 10, 10));

        HBox inAppLogViewRow = new HBox(5);
        Label inAppLogViewLabel = new Label("In App Log view?");
        CheckBox inAppLogViewCheckBox = new CheckBox();
        inAppLogViewCheckBox.setSelected(AppParams.getInAppLogView());
        inAppLogViewCheckBox.setOnMouseClicked(event -> {
            AppParams.setInAppLogView(((CheckBox) event.getSource()).isSelected());
            AppProperties.saveToXML();
        });

        inAppLogViewRow.getChildren().add(inAppLogViewLabel);
        inAppLogViewRow.getChildren().add(inAppLogViewCheckBox);

        rows.getChildren().add(inAppLogViewRow);

        anchorPane.getChildren().add(rows);

        return anchorPane;
    }

    public String getName() {
        return "Log Settings";
    }
}

