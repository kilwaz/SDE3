package sde.application.node.implementations;

import sde.application.GUI;
import sde.application.data.SavableAttribute;
import sde.application.data.export.Export;
import sde.application.data.export.ExportBuilder;
import sde.application.gui.Controller;
import sde.application.node.design.DrawableNode;
import sde.application.utils.NodeRunParams;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.apache.log4j.Logger;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExportNode extends DrawableNode {
    private static Logger log = Logger.getLogger(ExportNode.class);
    private String fileOutputDirectory = "";
    private String fileOutputName = "";
    private String constructedFileName = "";
    private String constructedDirectory = "";
    private TextField directoryField;
    private Label constructedFileNameLabel;

    // This will make a copy of the node passed to it
    public ExportNode(ExportNode exportNode) {
        this.setX(exportNode.getX());
        this.setY(exportNode.getY());
        this.setWidth(exportNode.getWidth());
        this.setHeight(exportNode.getHeight());
        this.setColor(exportNode.getColor());
        this.setScale(exportNode.getScale());
        this.setContainedText(exportNode.getContainedText());
//        this.setProgramUuid(exportNode.getProgramUuid());
        this.setNextNodeToRun(exportNode.getNextNodeToRun());

        this.setFileOutputDirectory(exportNode.getFileOutputDirectory());
        this.setFileOutputName(exportNode.getFileOutputName());
    }

    public ExportNode() {
        super();
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        if (nodeRunParams.getOneTimeVariable() instanceof Export) {
            Export export = (Export) nodeRunParams.getOneTimeVariable();

            String fileNameToUse;
            if (nodeRunParams.getVariable("fullFileName") != null) {
                fileNameToUse = (String) nodeRunParams.getVariable("fullFileName");
            } else if (nodeRunParams.getVariable("fileName") != null && nodeRunParams.getVariable("fileDirectory") != null) {
                fileNameToUse = buildConstructedFileName((String) nodeRunParams.getVariable("fileDirectory"), (String) nodeRunParams.getVariable("fileName"));
            } else {
                buildConstructedFileName();
                fileNameToUse = constructedFileName;
            }

            buildConstructedFileName();
            new ExportBuilder().export(export).saveLocation(new File(fileNameToUse)).construct();
        }
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        // DIRECTORY ROW
        Label directoryLabel = new Label();
        directoryLabel.setText("Output Directory: ");
        directoryLabel.setMinWidth(130);

        directoryField = new TextField();
        directoryField.setText(fileOutputDirectory);
        directoryField.setPrefWidth(250);
        directoryField.setOnAction(event -> {
            TextField sourceTextField = (TextField) event.getSource();
            fileOutputDirectory = sourceTextField.getText();
            buildConstructedFileName();
            save();
        });

        Button fileChooserButton = new Button();
        fileChooserButton.setText("Choose Output Directory...");
        fileChooserButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose Output Directory");
            File startDirectory = new File(fileOutputDirectory);
            if (startDirectory.exists()) {
                directoryChooser.setInitialDirectory(startDirectory);
            }
            File selectedDirectory = directoryChooser.showDialog(GUI.getInstance().getMainStage());

            if (selectedDirectory != null) {
                fileOutputDirectory = selectedDirectory.getAbsolutePath();
                fileOutputDirectory = fileOutputDirectory.replaceAll("\\\\", "/");
                directoryField.setText(fileOutputDirectory);
                buildConstructedFileName();
                save();
            }
        });

        HBox directoryRow = new HBox(5);
        directoryRow.setAlignment(Pos.CENTER_LEFT);
        directoryRow.getChildren().add(directoryLabel);
        directoryRow.getChildren().add(directoryField);
        directoryRow.getChildren().add(fileChooserButton);

        // OUTPUT FILE NAME
        Label outputFileNameLabel = new Label();
        outputFileNameLabel.setText("Output File Name: ");
        outputFileNameLabel.setMinWidth(130);

        TextField outputFileNameField = new TextField();
        outputFileNameField.setText(fileOutputName);
        outputFileNameField.setPrefWidth(250);
        outputFileNameField.setOnAction(event -> {
            TextField sourceTextField = (TextField) event.getSource();
            fileOutputName = sourceTextField.getText();
            buildConstructedFileName();
            save();
        });

        Label dateHelpLabel = new Label();
        dateHelpLabel.setText("Use [DATE] to replace with current date/time");

        HBox outputFileNameRow = new HBox(5);
        outputFileNameRow.setAlignment(Pos.CENTER_LEFT);
        outputFileNameRow.getChildren().add(outputFileNameLabel);
        outputFileNameRow.getChildren().add(outputFileNameField);
        outputFileNameRow.getChildren().add(dateHelpLabel);

        // CONSTRUCTED FILE NAME
        Label constructedFileNameLabel = new Label();
        constructedFileNameLabel.setText("Constructed File Name: ");
        constructedFileNameLabel.setMinWidth(130);

        this.constructedFileNameLabel = new Label();
        buildConstructedFileName();

        HBox constructedFileNameRow = new HBox(5);
        constructedFileNameRow.setAlignment(Pos.CENTER_LEFT);
        constructedFileNameRow.getChildren().add(constructedFileNameLabel);
        constructedFileNameRow.getChildren().add(this.constructedFileNameLabel);

        // ALL ROWS
        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        rows.getChildren().add(directoryRow);
        rows.getChildren().add(outputFileNameRow);
        rows.getChildren().add(constructedFileNameRow);

        anchorPane.getChildren().add(rows);

        return tab;
    }

    public String buildConstructedFileName() {
        return buildConstructedFileName(fileOutputDirectory, fileOutputName);
    }

    public String buildConstructedFileName(String fileDirectory, String fileName) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        Date date = new Date();
        String fileDate = dateFormat.format(date);

        constructedFileName = fileDirectory + "/" + fileName + ".xlsx";
        constructedFileName = constructedFileName.replace("[DATE]", fileDate);

        constructedDirectory = fileDirectory.replace("[DATE]", fileDate) + "/";

        updateConstructedFileNameLabel(constructedFileName);
        return constructedFileName;
    }

    public void updateConstructedFileNameLabel(String text) {
        class GUIUpdate implements Runnable {
            String text;

            GUIUpdate(String text) {
                this.text = text;
            }

            public void run() {
                if (constructedFileNameLabel != null) {
                    constructedFileNameLabel.setText(text);
                }
            }
        }

        Platform.runLater(new GUIUpdate(text));
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        // FileOutputDirectory
        SavableAttribute fileOutputDirectoryAttribute = SavableAttribute.create(SavableAttribute.class);
        fileOutputDirectoryAttribute.init("FileOutputDirectory", fileOutputDirectory.getClass().getName(), fileOutputDirectory, this);
        savableAttributes.add(fileOutputDirectoryAttribute);

        // FileOutputName
        SavableAttribute fileOutputNameAttribute = SavableAttribute.create(SavableAttribute.class);
        fileOutputNameAttribute.init("FileOutputName", fileOutputName.getClass().getName(), fileOutputName, this);
        savableAttributes.add(fileOutputNameAttribute);

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public String getFileOutputDirectory() {
        return fileOutputDirectory;
    }

    public void setFileOutputDirectory(String fileOutputDirectory) {
        this.fileOutputDirectory = fileOutputDirectory;
    }

    public String getFileOutputName() {
        return fileOutputName;
    }

    public void setFileOutputName(String fileOutputName) {
        this.fileOutputName = fileOutputName;
    }

    public String getConstructedFileName() {
        //buildConstructedFileName();
        return constructedFileName;
    }

    public String getConstructedDirectory() {
        //buildConstructedFileName();
        return constructedDirectory;
    }
}
