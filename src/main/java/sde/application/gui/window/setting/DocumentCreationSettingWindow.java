package sde.application.gui.window.setting;

import javafx.geometry.Pos;
import sde.application.utils.AppParams;
import sde.application.utils.AppProperties;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;

public class DocumentCreationSettingWindow extends SettingsPage {
    private static Logger log = Logger.getLogger(DocumentCreationSettingWindow.class);

    public DocumentCreationSettingWindow() {
        super();
    }

    public void setupChildren() {

    }

    public AnchorPane getInterface() {
        AnchorPane anchorPane = new AnchorPane();

        VBox rows = new VBox(5);
        rows.setPadding(new Insets(10, 10, 10, 10));

        HBox testDocOutputRow = new HBox(5);
        testDocOutputRow.setAlignment(Pos.CENTER_LEFT);
        Label testDocOutputLabel = new Label("Test document output path:");
        TextField testDocOutputField = new TextField();
        testDocOutputField.setText(AppParams.getTestDocOutputDir());
        testDocOutputField.setPrefWidth(200.0);
        testDocOutputField.setOnKeyReleased(event -> {
            AppParams.setTestDocOutputDir(((TextField) event.getSource()).getText());
            AppProperties.saveToXML();
        });

        testDocOutputRow.getChildren().add(testDocOutputLabel);
        testDocOutputRow.getChildren().add(testDocOutputField);


        HBox createTestDocRow = new HBox(5);
        createTestDocRow.setAlignment(Pos.CENTER_LEFT);
        CheckBox createTestDocCheck = new CheckBox("Create test documentation");
        createTestDocCheck.setSelected(AppParams.getCreateTestDocument());
        createTestDocCheck.setOnAction(event -> {
            AppParams.setCreateTestDocument(((CheckBox) event.getSource()).isSelected());
            AppProperties.saveToXML();
        });

        createTestDocRow.getChildren().add(createTestDocCheck);

        HBox recordScreenshotsRow = new HBox(5);
        recordScreenshotsRow.setAlignment(Pos.CENTER_LEFT);
        CheckBox recordScreenshotsCheck = new CheckBox("Record test screenshots");
        recordScreenshotsCheck.setSelected(AppParams.getRecordScreenshots());
        recordScreenshotsCheck.setOnAction(event -> {
            AppParams.setRecordScreenshots(((CheckBox) event.getSource()).isSelected());
            AppProperties.saveToXML();
        });

        recordScreenshotsRow.getChildren().add(recordScreenshotsCheck);

        rows.getChildren().add(testDocOutputRow);
        rows.getChildren().add(createTestDocRow);
        rows.getChildren().add(recordScreenshotsRow);

        anchorPane.getChildren().add(rows);

        return anchorPane;
    }

    public String getName() {
        return "Documentation Creation";
    }
}
