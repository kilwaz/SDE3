package application.gui.window;

import application.data.imports.ImportNodeColours;
import application.data.imports.ImportNodes;
import application.error.Error;
import application.gui.AceTextArea;
import application.gui.UI;
import application.utils.SDEThread;
import application.utils.XMLTransform;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ImportWindow extends Stage {

    private AceTextArea importTextArea;

    public ImportWindow() {
        init();
    }

    private static Logger log = Logger.getLogger(ImportWindow.class);

    private HBox headerButtons;
    private ProgressIndicator progressIndicator;

    private void init() {
        try {
            importTextArea = new AceTextArea("ace/mode/xml");

            UI.setAnchorMargins(importTextArea, 0.0, 0.0, 0.0, 0.0);

            importTextArea.setPrefHeight(900);

            headerButtons = new HBox(5);

            Button loadImportButton = new Button();
            loadImportButton.setText("Import File...");
            loadImportButton.setOnAction(event -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose Import File");

                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
                File file = fileChooser.showOpenDialog(this);

                if (file != null && file.exists()) {
                    try {
                        Document dom;
                        // Make an  instance of the DocumentBuilderFactory
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                        // use the factory to take an instance of the document builder
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        // parse using the builder to get the DOM mapping of the
                        // XML file
                        dom = db.parse(file);

                        importTextArea.setText(XMLTransform.writeXMLToString(dom));
                    } catch (ParserConfigurationException | SAXException | IOException ex) {
                        Error.IMPORT_PARSE.record().create(ex);
                    }
                }
            });

            Button importButton = new Button();
            importButton.setText("Run Import");
            importButton.setOnAction(event -> {
                Document document = XMLTransform.writeStringToXML(importTextArea.getText());
                if (document.getDocumentElement().getTagName().contains("NodeColours")) { // Check for node colours first as the second check checks for anything contain 'node'.
                    new SDEThread(new ImportNodeColours(document, this), "Importing Node Colours");
                } else if (document.getDocumentElement().getTagName().contains("Program") || document.getDocumentElement().getTagName().contains("Node")) {
                    new SDEThread(new ImportNodes(document, this), "Importing Node");
                }
            });

            headerButtons.getChildren().add(loadImportButton);
            headerButtons.getChildren().add(importButton);

            VBox rows = new VBox(5);
            rows.setPadding(new Insets(7, 11, 7, 11));
            rows.getChildren().add(headerButtons);
            rows.getChildren().add(importTextArea);

            AnchorPane exportOutputAnchor = new AnchorPane();

            UI.setAnchorMargins(exportOutputAnchor, 0.0, 0.0, 0.0, 0.0);
            UI.setAnchorMargins(rows, 0.0, 0.0, 0.0, 0.0);

            exportOutputAnchor.getChildren().add(rows);

            Scene newScene = new Scene(exportOutputAnchor, 900, 800);
            this.setScene(newScene);
            this.setTitle("Import");

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            Error.CREATE_IMPORT_WINDOW.record().create(ex);
        }
    }

    public void startImportProgress() {
        class GUIUpdate implements Runnable {
            GUIUpdate() {
            }

            public void run() {
                progressIndicator = new ProgressIndicator();
                headerButtons.getChildren().add(progressIndicator);
            }
        }

        Platform.runLater(new GUIUpdate());
    }

    public void updateImportProgress(Double progressValue) {
        class GUIUpdate implements Runnable {
            Double progressValue;

            GUIUpdate(Double progressValue) {
                this.progressValue = progressValue;
            }

            public void run() {
                progressIndicator.setProgress(progressValue);
            }
        }

        Platform.runLater(new GUIUpdate(progressValue));
    }

    public void endImportProgress() {
        class GUIUpdate implements Runnable {
            GUIUpdate() {
            }

            public void run() {
                headerButtons.getChildren().remove(progressIndicator);
                progressIndicator = null;
            }
        }

        Platform.runLater(new GUIUpdate());
    }

    public void closeWindow() {
        class GUIUpdate implements Runnable {
            private Stage stage;

            GUIUpdate(Stage stage) {
                this.stage = stage;
            }

            public void run() {
                stage.close();
            }
        }

        Platform.runLater(new GUIUpdate(this));
    }
}