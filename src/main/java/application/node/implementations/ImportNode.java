package application.node.implementations;

import application.Main;
import application.data.SavableAttribute;
import application.data.export.Export;
import application.data.imports.ImportBuilder;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.utils.NodeRunParams;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImportNode extends DrawableNode {
    private static Logger log = Logger.getLogger(ImportNode.class);
    private String fileImportName = "";
    private TextField fileImportField;

    // This will make a copy of the node passed to it
    public ImportNode(ImportNode importNode) {
        this.setX(importNode.getX());
        this.setY(importNode.getY());
        this.setWidth(importNode.getWidth());
        this.setHeight(importNode.getHeight());
        this.setColor(importNode.getColor());
        this.setScale(importNode.getScale());
        this.setContainedText(importNode.getContainedText());
        this.setNextNodeToRun(importNode.getNextNodeToRun());
    }

    public ImportNode() {
        super();
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        // DIRECTORY ROW
        Label importFileLabel = new Label();
        importFileLabel.setText("Import File: ");
        importFileLabel.setMinWidth(80);

        fileImportField = new TextField();
        fileImportField.setText(fileImportName);
        fileImportField.setPrefWidth(650);
        fileImportField.setOnAction(event -> {
            TextField sourceTextField = (TextField) event.getSource();
            fileImportName = sourceTextField.getText();
            save();
        });

        Button fileChooserButton = new Button();
        fileChooserButton.setText("Choose Import File...");
        fileChooserButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Import File...");
            File fileImport = new File(fileImportName);

            if (fileImport.exists()) {
                fileChooser.setInitialDirectory(fileImport.getParentFile());
            }

            File selectedFile = fileChooser.showOpenDialog(Main.getInstance().getMainStage());

            if (selectedFile != null) {
                fileImportName = selectedFile.getAbsolutePath();
                fileImportName = fileImportName.replaceAll("\\\\", "/");
                fileImportField.setText(fileImportName);
                save();
            }
        });

        HBox fileImportRow = new HBox(5);
        fileImportRow.setAlignment(Pos.CENTER_LEFT);
        fileImportRow.getChildren().add(importFileLabel);
        fileImportRow.getChildren().add(fileImportField);
        fileImportRow.getChildren().add(fileChooserButton);

        // ALL ROWS
        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        rows.getChildren().add(fileImportRow);

        anchorPane.getChildren().add(rows);

        return tab;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        // FileInputName
        SavableAttribute fileImportNameAttribute = SavableAttribute.create(SavableAttribute.class);
        fileImportNameAttribute.init("FileImportName", fileImportName.getClass().getName(), fileImportName, this);
        savableAttributes.add(fileImportNameAttribute);

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public Export getImport() {
        ImportBuilder importBuilder = new ImportBuilder().loadFile(new File(fileImportName)).construct();
        return importBuilder.getExport();
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        ImportBuilder importBuilder = new ImportBuilder().loadFile(new File(fileImportName)).construct();

        nodeRunParams.setOneTimeVariable(importBuilder.getExport());
    }

    public String getFileImportName() {
        return fileImportName;
    }

    public void setFileImportName(String fileImportName) {
        this.fileImportName = fileImportName;
    }
}
