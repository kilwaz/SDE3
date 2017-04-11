package sde.application.gui.window.setting;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import sde.application.error.Error;
import sde.application.gui.window.setting.browsers.ChromeSettings;
import sde.application.gui.window.setting.browsers.FirefoxSettings;
import sde.application.gui.window.setting.browsers.InternetExplorerSettings;
import sde.application.gui.window.setting.browsers.OperaSettings;
import sde.application.utils.AppParams;
import sde.application.utils.AppProperties;

public class BrowserSettingWindow extends SettingsPage {
    private static Logger log = Logger.getLogger(BrowserSettingWindow.class);

    public BrowserSettingWindow() {
        super();
    }

    public void setupChildren() {
        getChildren().add(new ChromeSettings());
        getChildren().add(new InternetExplorerSettings());
        getChildren().add(new FirefoxSettings());
        getChildren().add(new OperaSettings());
    }

    public AnchorPane getInterface() {
        VBox rows = new VBox(5);
        rows.setPadding(new Insets(10, 10, 10, 10));

        HBox browserRetryCountRow = new HBox(5);
        browserRetryCountRow.setAlignment(Pos.CENTER_LEFT);

        Label browserRetryCountLabel = new Label("Browser retry count:");
        TextField browserRetryCountValue = new TextField();
        browserRetryCountValue.setText(AppParams.getBrowserDefaultRetryCount().toString());
        browserRetryCountValue.setPrefWidth(100.0);
        browserRetryCountValue.setOnKeyReleased(event -> {
            try {
                AppParams.setBrowserDefaultRetryCount(Integer.parseInt(((TextField) event.getSource()).getText()));
                AppProperties.saveToXML();
            } catch (NumberFormatException ex) {
                Error.NUMBER_FORMAT_ERROR.record().additionalInformation("Trying to parse value: '" + ((TextField) event.getSource()).getText() + "' from browser retry settings").hideStackInLog().create(ex);
                // In this case we don't really want to report the error too much
            }
        });

        browserRetryCountRow.getChildren().add(browserRetryCountLabel);
        browserRetryCountRow.getChildren().add(browserRetryCountValue);

        rows.getChildren().add(browserRetryCountRow);

        StackPane root = new StackPane();

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(rows);
        root.getChildren().add(anchorPane);

        return anchorPane;
    }

    public String getName() {
        return "Browser";
    }
}
