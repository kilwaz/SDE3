package application.gui.window;

import application.data.model.dao.NodeColourDAO;
import application.data.xml.DrawableNodeXML;
import application.data.xml.NodeColoursXML;
import application.data.xml.ProgramXML;
import application.error.Error;
import application.gui.AceTextArea;
import application.gui.Program;
import application.node.design.DrawableNode;
import application.utils.XMLTransform;
import application.utils.managers.SessionManager;
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
    public final static Integer EXPORT_NODE = 1;
    public final static Integer EXPORT_PROGRAM = 2;
    public final static Integer EXPORT_NODE_COLOURS = 3;

    private Integer exportType;
    private List<DrawableNode> selectedNodes;

    private static Logger log = Logger.getLogger(ExportWindow.class);

    public ExportWindow(Integer exportType) {
        this.exportType = exportType;
        init();
    }

    private void init() {
        try {
            Program program = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
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
                if (exportType.equals(EXPORT_NODE)) {
                    for (DrawableNode drawableNode : selectedNodes) {
                        fileName = drawableNode.getContainedText();
                    }
                } else if (exportType.equals(EXPORT_PROGRAM)) {
                    if (program != null) {
                        fileName = program.getName();
                    }
                } else if (exportType.equals(EXPORT_NODE_COLOURS)) {
                    if (program != null) {
                        fileName = "Node Colours";
                    }
                }

                fileChooser.setInitialFileName(fileName + ".xml");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
                File file = fileChooser.showSaveDialog(this);
                if (file != null) {
                    if (exportType.equals(EXPORT_NODE)) {
                        for (DrawableNode drawableNode : selectedNodes) {
                            DrawableNodeXML drawableNodeXML = new DrawableNodeXML(drawableNode);
                            XMLTransform.writeXMLToFile(drawableNodeXML.getXMLRepresentation(), file.getAbsolutePath());
                        }
                    } else if (exportType.equals(EXPORT_PROGRAM)) {
                        ProgramXML programXML = new ProgramXML(SessionManager.getInstance().getCurrentSession().getSelectedProgram());
                        XMLTransform.writeXMLToFile(programXML.getXMLRepresentation(), file.getAbsolutePath());
                    } else if (exportType.equals(EXPORT_NODE_COLOURS)) {
                        NodeColourDAO nodeColourDAO = new NodeColourDAO();
                        NodeColoursXML nodeColoursXML = new NodeColoursXML(nodeColourDAO.getNodeColours());
                        XMLTransform.writeXMLToFile(nodeColoursXML.getXMLRepresentation(), file.getAbsolutePath());
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
                for (DrawableNode drawableNode : selectedNodes) {
                    DrawableNodeXML drawableNodeXML = new DrawableNodeXML(drawableNode);
                    exportTextArea.setText(exportTextArea.getText() + "\r\n" + XMLTransform.writeXMLToString(drawableNodeXML.getXMLRepresentation()));
                }
                this.setTitle("Export Node " + (selectedNodes.size() > 0 ? selectedNodes.get(0).getContainedText() : ""));
            } else if (exportType.equals(EXPORT_PROGRAM)) {
                Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
                ProgramXML programXML = new ProgramXML(selectedProgram);
                exportTextArea.setText(XMLTransform.writeXMLToString(programXML.getXMLRepresentation()));
                this.setTitle("Export Program " + selectedProgram.getName());
            } else if (exportType.equals(EXPORT_NODE_COLOURS)) {
                NodeColourDAO nodeColourDAO = new NodeColourDAO();
                NodeColoursXML nodeColoursXML = new NodeColoursXML(nodeColourDAO.getNodeColours());
                exportTextArea.setText(exportTextArea.getText() + "\r\n" + XMLTransform.writeXMLToString(nodeColoursXML.getXMLRepresentation()));

                this.setTitle("Export Node Colours");
            }

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            Error.CREATE_EXPORT_WINDOW.record().create(ex);
        }
    }
}