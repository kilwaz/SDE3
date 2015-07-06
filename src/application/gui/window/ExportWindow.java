package application.gui.window;

import application.data.DataBank;
import application.gui.AceTextArea;
import application.gui.Program;
import application.node.design.DrawableNode;
import application.utils.XMLTransform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ExportWindow extends Stage {
    public static Integer EXPORT_NODE = 1;
    public static Integer EXPORT_PROGRAM = 2;

    private Integer exportType;
    private List<DrawableNode> selectedNodes;

    private static Logger log = Logger.getLogger(ExportWindow.class);

    public ExportWindow(Integer exportType) {
        this.exportType = exportType;
        init();
    }

    private void init() {
        try {
            Program program = DataBank.currentlyEditProgram;
            selectedNodes = new ArrayList<>();
            if (program != null) {
                selectedNodes = program.getFlowController().getSelectedNodes();
            }

            AceTextArea exportTextArea = new AceTextArea("ace/mode/xml");

            AnchorPane.setLeftAnchor(exportTextArea, 0.0);
            AnchorPane.setRightAnchor(exportTextArea, 0.0);
            AnchorPane.setTopAnchor(exportTextArea, 0.0);
            AnchorPane.setBottomAnchor(exportTextArea, 0.0);

            exportTextArea.setPrefHeight(900);

            HBox headerButtons = new HBox(5);

            Button saveButton = new Button();
            saveButton.setText("Save...");
            saveButton.setOnAction(event -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");

                String fileName = "Blank Node";
                for (DrawableNode drawableNode : selectedNodes) {
                    fileName = drawableNode.getContainedText();
                }

                fileChooser.setInitialFileName(fileName + ".xml");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
                File file = fileChooser.showSaveDialog(this);
                if (file != null) {
                    for (DrawableNode drawableNode : selectedNodes) {
                        XMLTransform.writeXMLToFile(drawableNode.getXMLRepresentation(), file.getAbsolutePath());
                    }
                }
            });

            headerButtons.getChildren().add(saveButton);

            VBox rows = new VBox(5);
            rows.setPadding(new Insets(7, 11, 7, 11));
            rows.getChildren().add(headerButtons);
            rows.getChildren().add(exportTextArea);

            AnchorPane exportOutputAnchor = new AnchorPane();

            AnchorPane.setLeftAnchor(exportOutputAnchor, 0.0);
            AnchorPane.setRightAnchor(exportOutputAnchor, 0.0);
            AnchorPane.setTopAnchor(exportOutputAnchor, 0.0);
            AnchorPane.setBottomAnchor(exportOutputAnchor, 0.0);

            AnchorPane.setLeftAnchor(rows, 0.0);
            AnchorPane.setRightAnchor(rows, 0.0);
            AnchorPane.setTopAnchor(rows, 0.0);
            AnchorPane.setBottomAnchor(rows, 0.0);

            exportOutputAnchor.getChildren().add(rows);

            Scene newScene = new Scene(exportOutputAnchor, 900, 800);
            this.setScene(newScene);

            if (exportType.equals(EXPORT_NODE)) {
                this.setTitle("Export Node");
                for (DrawableNode drawableNode : selectedNodes) {
                    exportTextArea.setText(XMLTransform.writeXMLToString(drawableNode.getXMLRepresentation()));
                }
            } else if (exportType.equals(EXPORT_PROGRAM)) {
                this.setTitle("Export Program");
            }

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            log.error(ex);
        }
    }
}