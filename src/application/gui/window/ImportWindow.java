package application.gui.window;

import application.data.DataBank;
import application.gui.AceTextArea;
import application.gui.Controller;
import application.gui.Program;
import application.node.design.DrawableNode;
import application.node.implementations.DataTableNode;
import application.node.implementations.InputNode;
import application.node.implementations.SwitchNode;
import application.node.implementations.TriggerNode;
import application.node.objects.datatable.DataTableRow;
import application.utils.SDEUtils;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

public class ImportWindow extends Stage {

    private AceTextArea importTextArea;

    public ImportWindow() {
        init();
    }

    private static Logger log = Logger.getLogger(ImportWindow.class);

    private void init() {
        try {
            importTextArea = new AceTextArea("ace/mode/xml");

            AnchorPane.setLeftAnchor(importTextArea, 0.0);
            AnchorPane.setRightAnchor(importTextArea, 0.0);
            AnchorPane.setTopAnchor(importTextArea, 0.0);
            AnchorPane.setBottomAnchor(importTextArea, 0.0);

            importTextArea.setPrefHeight(900);

            HBox headerButtons = new HBox(5);

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
                        log.error(ex);
                    }
                }
            });

            Button importButton = new Button();
            importButton.setText("Run Import");
            importButton.setOnAction(event -> {
                Document document = XMLTransform.writeStringToXML(importTextArea.getText());

                Element element = document.getDocumentElement();

                if (element.getTagName().contains("Node")) {
                    importNode(DataBank.currentlyEditProgram, element);
                } else if (element.getTagName().equals("Program")) {
                    String programName = getTextValue("", element, "ProgramName");
                    Program newProgram = DataBank.createNewProgram(programName);
                    Controller.getInstance().addNewProgram(newProgram);

                    NodeList programChildNodes = element.getChildNodes();
                    for (int i = 0; i < programChildNodes.getLength(); i++) {
                        if (programChildNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element programTopElements = (Element) programChildNodes.item(i);

                            if (programTopElements.getTagName().equals("Nodes")) {
                                NodeList childNodeList = programTopElements.getChildNodes();

                                for (int n = 0; n < childNodeList.getLength(); n++) {
                                    if (childNodeList.item(n).getNodeType() == Node.ELEMENT_NODE) {
                                        Element nodeElement = (Element) childNodeList.item(n);

                                        if (nodeElement.getTagName().contains("Node")) {
                                            importNode(newProgram, nodeElement);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Closes the import window and update the display to show the new node, also check all connections
                Controller.getInstance().updateCanvasControllerLater();
                Program program = DataBank.currentlyEditProgram;
                if (program != null) {
                    program.getFlowController().checkConnections();
                }
                this.close();
            });

            headerButtons.getChildren().add(loadImportButton);
            headerButtons.getChildren().add(importButton);

            VBox rows = new VBox(5);
            rows.setPadding(new Insets(7, 11, 7, 11));
            rows.getChildren().add(headerButtons);
            rows.getChildren().add(importTextArea);

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
            this.setTitle("Import");

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    private void importNode(Program program, Element element) {
        DrawableNode importedNode = program.getFlowController().createNewNode(-1, program.getId(), element.getTagName(), false);

        NodeList childNodeList = element.getChildNodes();
        for (int i = 0; i < childNodeList.getLength(); i++) {
            if (childNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element nodeTopElements = (Element) childNodeList.item(i);

                if ("Variable".equals(nodeTopElements.getTagName())) {
                    String variableName = getTextValue("", nodeTopElements, "VariableName");
                    String className = getTextValue("", nodeTopElements, "ClassName");
                    String variableValue = getTextValue("", nodeTopElements, "VariableValue");

                    if (!"Id".equals(variableName)) { // Here we don't want to keep the same Id as it may cause conflicts
                        Method method;
                        try {
                            if ("java.lang.Double".equals(className)) {
                                Double doubleValue = Double.parseDouble(variableValue);

                                method = importedNode.getClass().getMethod("set" + variableName, Class.forName(className));
                                method.invoke(importedNode, doubleValue);
                            } else if ("java.lang.String".equals(className)) {
                                String stringValue = variableValue;

                                method = importedNode.getClass().getMethod("set" + variableName, Class.forName(className));
                                method.invoke(importedNode, stringValue);
                            } else if ("java.lang.Integer".equals(className)) {
                                Integer integerValue = Integer.parseInt(variableValue);

                                method = importedNode.getClass().getMethod("set" + variableName, Class.forName(className));
                                method.invoke(importedNode, integerValue);
                            }
                        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException ex) {
                            log.error(ex);
                        }
                    }
                } else if ("Inputs".equals(nodeTopElements.getTagName())) {
                    DataBank.saveNode(importedNode); // We need to save the node to set the ID first before we can add anything else

                    NodeList inputNodesList = nodeTopElements.getChildNodes();
                    InputNode inputNode = (InputNode) importedNode;
                    for (Integer inputsCount = 0; inputsCount < inputNodesList.getLength(); inputsCount++) {
                        if (inputNodesList.item(inputsCount).getNodeType() == Node.ELEMENT_NODE) {
                            Element inputElement = (Element) inputNodesList.item(inputsCount);

                            String inputVariableName = getTextValue("", inputElement, "VariableName");
                            String inputVariableValue = getTextValue("", inputElement, "VariableValue");

                            DataBank.createNewInput(inputVariableName, inputVariableValue, inputNode);
                        }
                    }
                } else if ("Triggers".equals(nodeTopElements.getTagName())) {
                    DataBank.saveNode(importedNode); // We need to save the node to set the ID first before we can add anything else

                    NodeList triggersNodeList = nodeTopElements.getChildNodes();
                    TriggerNode triggerNode = (TriggerNode) importedNode;
                    for (Integer triggerCounts = 0; triggerCounts < triggersNodeList.getLength(); triggerCounts++) {
                        if (triggersNodeList.item(triggerCounts).getNodeType() == Node.ELEMENT_NODE) {
                            Element triggerElement = (Element) triggersNodeList.item(triggerCounts);

                            String triggerWatch = getTextValue("", triggerElement, "Watch");
                            String triggerWhen = getTextValue("", triggerElement, "When");
                            String triggerThen = getTextValue("", triggerElement, "Then");

                            DataBank.createNewTrigger(triggerWatch, triggerWhen, triggerThen, triggerNode);
                        }
                    }
                } else if ("Switches".equals(nodeTopElements.getTagName())) {
                    DataBank.saveNode(importedNode); // We need to save the node to set the ID first before we can add anything else

                    NodeList switchesNodeList = nodeTopElements.getChildNodes();
                    SwitchNode switchNode = (SwitchNode) importedNode;
                    for (Integer switchCount = 0; switchCount < switchesNodeList.getLength(); switchCount++) {
                        if (switchesNodeList.item(switchCount).getNodeType() == Node.ELEMENT_NODE) {
                            Element switchElement = (Element) switchesNodeList.item(switchCount);

                            String switchTarget = getTextValue("", switchElement, "Target");
                            Boolean switchEnabled = Boolean.parseBoolean(getTextValue("", switchElement, "Enabled"));

                            DataBank.createNewSwitch(switchTarget, switchEnabled, switchNode);
                        }
                    }
                } else if ("DataTableData".equals(nodeTopElements.getTagName())) {
                    DataBank.saveNode(importedNode); // We need to save the node to set the ID first before we can add anything else

                    NodeList dataTableDataRowList = nodeTopElements.getChildNodes();
                    DataTableNode dataTableNode = (DataTableNode) importedNode;
                    for (Integer rowCount = 0; rowCount < dataTableDataRowList.getLength(); rowCount++) {
                        if (dataTableDataRowList.item(rowCount).getNodeType() == Node.ELEMENT_NODE) {
                            DataTableRow dataTableRow = DataBank.createNewDataTableRow(dataTableNode);
                            Element dataTableRowElement = (Element) dataTableDataRowList.item(rowCount);

                            NodeList dataTableDataValueList = dataTableRowElement.getChildNodes();
                            for (Integer valueCount = 0; valueCount < dataTableDataValueList.getLength(); valueCount++) {
                                if (dataTableDataValueList.item(valueCount).getNodeType() == Node.ELEMENT_NODE) {
                                    Element dataTableValueElement = (Element) dataTableDataValueList.item(valueCount);

                                    String dataKey = getTextValue("", dataTableValueElement, "DataKey");
                                    String dataValue = getTextValue("", dataTableValueElement, "DataValue");

                                    dataTableRow.addDataTableValue(DataBank.createNewDataTableValue(dataTableRow, dataKey, dataValue));
                                }
                            }
                        }
                    }
                }
            }
        }

        DataBank.saveNode(importedNode);
    }

    private static String getTextValue(String def, Element element, String tag) {
        String value = def;
        NodeList nl;
        nl = element.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return SDEUtils.unescapeXMLCData(value);
    }
}