package application.gui.window;

import application.error.Error;
import application.gui.UI;
import application.utils.managers.StatisticsManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import org.apache.log4j.Logger;

import java.net.URL;

public class StatisticsWindow extends SDEWindow {
    private static Logger log = Logger.getLogger(ErrorWindow.class);

    public StatisticsWindow() {
        super();
        init();
    }

    private void init() {
        try {
            StatisticsManager statisticsManager = StatisticsManager.getInstance();

            //this.initStyle(StageStyle.UTILITY);  // No max or min buttons

            AnchorPane statisticsAnchor = new AnchorPane();
            statisticsAnchor.setPadding(new Insets(7, 11, 7, 11));

            UI.setAnchorMargins(statisticsAnchor, 0.0, 0.0, 0.0, 0.0);

            VBox vBox = new VBox(5);
            vBox.setPadding(new Insets(10, 10, 10, 10));

            vBox.getChildren().add(buildStatisticRow("Session Requests", Bindings.format("%,d", statisticsManager.getSessionStatisticStore().requestsProperty())));
            vBox.getChildren().add(buildStatisticRow("Session Up Time", Bindings.format("%s", statisticsManager.getSessionStatisticStore().upTimeFormattedProperty())));
            vBox.getChildren().add(buildStatisticRow("Session Request Size", Bindings.format("%s", statisticsManager.getSessionStatisticStore().requestSizeFormattedProperty())));
            vBox.getChildren().add(buildStatisticRow("Session Response Size", Bindings.format("%s", statisticsManager.getSessionStatisticStore().responseSizeFormattedProperty())));
            vBox.getChildren().add(buildStatisticRow("Session Total Commands", Bindings.format("%,d", statisticsManager.getSessionStatisticStore().commandsProperty())));
            vBox.getChildren().add(buildStatisticRow("Session Programs Starts", Bindings.format("%,d", statisticsManager.getSessionStatisticStore().programsStartedProperty())));
            vBox.getChildren().add(new Label()); // Spacer
            vBox.getChildren().add(buildStatisticRow("Total Requests", Bindings.format("%,d", statisticsManager.getTotalStatisticStore().requestsProperty())));
            vBox.getChildren().add(buildStatisticRow("Total Up Time", Bindings.format("%s", statisticsManager.getTotalStatisticStore().upTimeFormattedProperty())));
            vBox.getChildren().add(buildStatisticRow("Total Request Size", Bindings.format("%s", statisticsManager.getTotalStatisticStore().requestSizeFormattedProperty())));
            vBox.getChildren().add(buildStatisticRow("Total Response Size", Bindings.format("%s", statisticsManager.getTotalStatisticStore().responseSizeFormattedProperty())));
            vBox.getChildren().add(buildStatisticRow("Total Commands", Bindings.format("%,d", statisticsManager.getTotalStatisticStore().commandsProperty())));
            vBox.getChildren().add(buildStatisticRow("Total Application Starts", Bindings.format("%,d", statisticsManager.getTotalStatisticStore().applicationStartsProperty())));
            vBox.getChildren().add(buildStatisticRow("Total Programs Starts", Bindings.format("%,d", statisticsManager.getTotalStatisticStore().programsStartedProperty())));

            statisticsAnchor.getChildren().add(vBox);

            Scene newScene = new Scene(statisticsAnchor, 220, 350);
            this.setScene(newScene);

            this.setTitle("Application Statistics");

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            Error.CREATE_ERROR_WINDOW.record().create(ex);
        }
    }

    private HBox buildStatisticRow(String name, StringExpression property) {
        Label nameLabel = new Label(name + ":");

        Label valueLabel = new Label();
        valueLabel.textProperty().bind(property);

        HBox hBox = new HBox(5);
        hBox.getChildren().addAll(nameLabel, valueLabel);

        return hBox;
    }
}