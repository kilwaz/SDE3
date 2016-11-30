package application.gui.window;

import application.data.imports.ImportNodeColours;
import application.data.imports.ImportNodes;
import application.data.imports.ImportTask;
import application.error.Error;
import application.gui.UI;
import application.utils.SDEThread;
import application.utils.XMLTransform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.controlsfx.control.TaskProgressView;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ImportWindow extends SDEWindow {
    private static Logger log = Logger.getLogger(ImportWindow.class);
    private HBox headerButtons;
    private TaskProgressView<ImportTask> taskTaskProgressView = null;

    public ImportWindow() {
        super();
        init();
    }

    private void init() {
        try {
            taskTaskProgressView = new TaskProgressView<>();
            taskTaskProgressView.setGraphicFactory(ImportTask::getStatusImage);
            taskTaskProgressView.setPrefHeight(Integer.MAX_VALUE);

            headerButtons = new HBox(5);

            Button loadImportButton = new Button();
            loadImportButton.setText("Import File...");
            loadImportButton.setOnAction(event -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose Import File");

                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
                List<File> fileList = fileChooser.showOpenMultipleDialog(this);

                if (fileList != null) {
                    for (File file : fileList) {
                        if (file != null && file.exists()) {
                            try {
                                Document dom;
                                // Make an instance of the DocumentBuilderFactory
                                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                                // use the factory to take an instance of the document builder
                                DocumentBuilder db = dbf.newDocumentBuilder();
                                // parse using the builder to get the DOM mapping of the
                                // XML file
                                dom = db.parse(file);

                                // Create and start the UI task to track this import
                                ImportTask importTask = new ImportTask(XMLTransform.writeXMLToString(dom), file.getName());
                                taskTaskProgressView.getTasks().add(importTask);
                                Thread th = new Thread(importTask);
                                th.setDaemon(true);
                                th.start();
                            } catch (ParserConfigurationException | SAXException | IOException ex) {
                                Error.IMPORT_PARSE.record().create(ex);
                            }
                        }
                    }
                }
            });

            Button importButton = new Button();
            importButton.setText("Run Import");
            importButton.setOnAction(event -> {
                for (ImportTask importTask : taskTaskProgressView.getTasks()) {
                    String xml = importTask.getXml();
                    if (!xml.isEmpty()) {
                        Document document = XMLTransform.writeStringToXML(xml);
                        if (document.getDocumentElement().getTagName().contains("NodeColours")) { // Check for node colours first as the second check checks for anything contain 'node'.
                            new SDEThread(new ImportNodeColours(document, importTask), "Importing Node Colours", null, true);
                        } else if (document.getDocumentElement().getTagName().contains("Program") || document.getDocumentElement().getTagName().contains("Node")) {
                            new SDEThread(new ImportNodes(document, importTask), "Importing Node", null, true);
                        }
                    }
                }
            });

            headerButtons.getChildren().add(loadImportButton);
            headerButtons.getChildren().add(importButton);

            VBox rows = new VBox(5);
            rows.setPadding(new Insets(7, 11, 7, 11));
            rows.getChildren().add(headerButtons);
            rows.getChildren().add(taskTaskProgressView);

            AnchorPane exportOutputAnchor = new AnchorPane();

            UI.setAnchorMargins(exportOutputAnchor, 0.0, 0.0, 0.0, 0.0);
            UI.setAnchorMargins(rows, 0.0, 0.0, 0.0, 0.0);

            exportOutputAnchor.getChildren().add(rows);

            createScene(exportOutputAnchor, 700, 400);
            this.setTitle("Import");

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            Error.CREATE_IMPORT_WINDOW.record().create(ex);
        }
    }
}