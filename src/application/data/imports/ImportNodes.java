package application.data.imports;

import application.data.DataBank;
import application.error.*;
import application.error.Error;
import application.gui.Controller;
import application.gui.Program;
import application.gui.window.ImportWindow;
import application.node.design.DrawableNode;
import application.node.implementations.DataTableNode;
import application.node.implementations.InputNode;
import application.node.implementations.SwitchNode;
import application.node.implementations.TriggerNode;
import application.node.objects.datatable.DataTableRow;
import application.utils.SDERunnable;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ImportNodes extends SDERunnable {
    private static Logger log = Logger.getLogger(ImportNodes.class);
    private Document document;
    private ImportWindow importWindow;

    public ImportNodes(Document document, ImportWindow importWindow) {
        this.document = document;
        this.importWindow = importWindow;
    }

    public void threadRun() {
        Element element = document.getDocumentElement();
        if (importWindow != null) {
            importWindow.startImportProgress();
        }
        if (element.getTagName().contains("Node")) {
            importNode(DataBank.currentlyEditProgram, element);
            if (importWindow != null) {
                importWindow.updateImportProgress(0.5);
            }
        } else if (element.getTagName().equals("Program")) {
            String programName = ImportNodes.getTextValue("", element, "ProgramName");
            Program newProgram = DataBank.createNewProgram(programName);
            Controller.getInstance().addNewProgram(newProgram);

            NodeList programChildNodes = element.getChildNodes();
            for (int i = 0; i < programChildNodes.getLength(); i++) {
                if (programChildNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element programTopElements = (Element) programChildNodes.item(i);

                    if (programTopElements.getTagName().equals("Nodes")) {
                        NodeList childNodeList = programTopElements.getChildNodes();

                        List<Element> nodesToProcess = new ArrayList<>();
                        for (int n = 0; n < childNodeList.getLength(); n++) {
                            if (childNodeList.item(n).getNodeType() == Node.ELEMENT_NODE) {
                                Element nodeElement = (Element) childNodeList.item(n);

                                if (nodeElement.getTagName().contains("Node")) {
                                    nodesToProcess.add(nodeElement);
                                }
                            }
                        }

                        Double importedCount = 0.0;
                        for (Element nodeElement : nodesToProcess) {
                            if (importWindow != null) {
                                importWindow.updateImportProgress(importedCount / nodesToProcess.size());
                            }
                            importNode(newProgram, nodeElement);
                            importedCount++;
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

        if (importWindow != null) {
            importWindow.endImportProgress();
            importWindow.closeWindow();
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
                            Error.IMPORT_NODE.record().create(ex);
                        }
                    }
                } else if ("IsStartNode".equals(nodeTopElements.getTagName())) {
                    program.getFlowController().setStartNode(importedNode);
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
