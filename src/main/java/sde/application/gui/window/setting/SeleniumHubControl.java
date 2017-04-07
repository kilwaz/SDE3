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

public class SeleniumHubControl extends SettingsPage {
    private static Logger log = Logger.getLogger(SeleniumHubControl.class);

    public SeleniumHubControl() {
        super();
    }

    public AnchorPane getInterface() {
        AnchorPane anchorPane = new AnchorPane();

        VBox rows = new VBox(40);
        rows.setPadding(new Insets(10, 10, 10, 10));

        HBox manageSeleniumHubRow = new HBox(5);
        Label manageSeleniumHubLabel = new Label("Auto manage Selenium hub?");
        CheckBox manageSeleniumHubCheckBox = new CheckBox();
        manageSeleniumHubCheckBox.setSelected(AppParams.getAutoManageSeleniumHub());
        manageSeleniumHubCheckBox.setOnMouseClicked(event -> {
            AppParams.setAutoManageSeleniumHub(((CheckBox) event.getSource()).isSelected());
            AppProperties.saveToXML();
        });

        manageSeleniumHubRow.getChildren().add(manageSeleniumHubLabel);
        manageSeleniumHubRow.getChildren().add(manageSeleniumHubCheckBox);

        rows.getChildren().add(manageSeleniumHubRow);

        anchorPane.getChildren().add(rows);

        return anchorPane;
    }

    public String getName() {
        return "Selenium Hub";
    }
}