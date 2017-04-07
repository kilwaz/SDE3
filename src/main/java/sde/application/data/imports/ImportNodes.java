package sde.application.data.imports;

import sde.application.error.Error;
import sde.application.gui.Controller;
import sde.application.gui.Program;
import sde.application.node.design.DrawableNode;
import sde.application.node.implementations.DataTableNode;
import sde.application.node.implementations.InputNode;
import sde.application.node.implementations.SwitchNode;
import sde.application.node.implementations.TriggerNode;
import sde.application.node.objects.Input;
import sde.application.node.objects.Switch;
import sde.application.node.objects.Trigger;
import sde.application.node.objects.datatable.DataTableRow;
import sde.application.node.objects.datatable.DataTableValue;
import sde.application.utils.SDERunnable;
import sde.application.utils.SDEUtils;
import sde.application.utils.managers.DatabaseTransactionManager;
import sde.application.utils.managers.SessionManager;
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
    private ImportTask importTask = null;

    public ImportNodes(Document document, ImportTask importTask) {
        this.document = document;
        this.importTask = importTask;
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

    public void threadRun() {
        Element element = document.getDocumentElement();
        if (element.getTagName().contains("Node")) {
            importNode(SessionManager.getInstance().getCurrentSession().getSelectedProgram(), element);
        } else if (element.getTagName().equals("Program")) {
            importTask.started();
            String programName = ImportNodes.getTextValue("", element, "ProgramName");
            String lockedStatus = ImportNodes.getTextValue("", element, "Locked");

            Program newProgram = Program.create(Program.class);
            newProgram.setName(programName);
            newProgram.setLocked("Y".equals(lockedStatus));

            NodeList programChildNodes = element.getChildNodes();
            List<Element> nodesToProcess = new ArrayList<>();
            for (int i = 0; i < programChildNodes.getLength(); i++) {
                if (programChildNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element programTopElements = (Element) programChildNodes.item(i);

                    if (programTopElements.getTagName().equals("Nodes")) {
                        NodeList childNodeList = programTopElements.getChildNodes();

                        for (int n = 0; n < childNodeList.getLength(); n++) {
                            if (childNodeList.item(n).getNodeType() == Node.ELEMENT_NODE) {
                                Element nodeElement = (Element) childNodeList.item(n);

                                if (nodeElement.getTagName().contains("Node")) {
                                    nodesToProcess.add(nodeElement);
                                }
                            }
                        }
                    }
                }
            }

            // Start the actual import
            Integer importedCount = 0;
            importTask.setMaximum(nodesToProcess.size());
            for (Element nodeElement : nodesToProcess) {
                importTask.updateProgress(importedCount);
                importNode(newProgram, nodeElement);
                importedCount++;
            }

            newProgram.setParentUser(SessionManager.getInstance().getCurrentSession().getUser());
            newProgram.save();
            Controller.getInstance().updateCanvasControllerLater();

            // We need to reload the program in order to correctly set it up to be used straight away
            Program reloadedProgram = Program.load(newProgram.getUuid(), Program.class);
            Controller.getInstance().addNewProgram(reloadedProgram);

            importTask.setIsFinished(true);
        }
    }

    private void importNode(Program program, Element element) {
        DrawableNode importedNode = null;
        try {
            Class<DrawableNode> clazz = (Class<DrawableNode>) Class.forName("sde.application.node.implementations." + element.getTagName());

            importedNode = DrawableNode.create(clazz);
            importedNode.setProgram(program);
        } catch (ClassNotFoundException ex) {
            Error.CREATE_NEW_NODE.record().create(ex);
        }

        if (importedNode != null) {
            NodeList childNodeList = element.getChildNodes();
            for (int i = 0; i < childNodeList.getLength(); i++) {
                if (childNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element nodeTopElements = (Element) childNodeList.item(i);

                    if ("Variable".equals(nodeTopElements.getTagName())) {
                        String variableName = getTextValue("", nodeTopElements, "VariableName");
                        String className = getTextValue("", nodeTopElements, "ClassName");
                        String variableValue = getTextValue("", nodeTopElements, "VariableValue");

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
                            } else if ("java.lang.Boolean".equals(className)) {
                                Boolean booleanValue = Boolean.parseBoolean(variableValue);

                                method = importedNode.getClass().getMethod("set" + variableName, Class.forName(className));
                                method.invoke(importedNode, booleanValue);
                            }
                        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException ex) {
                            Error.IMPORT_NODE.record().additionalInformation("Method: " + "set" + variableName).create(ex);
                        }
                    } else if ("IsStartNode".equals(nodeTopElements.getTagName())) {
                        program.setStartNode(importedNode);
                    } else if ("TestInputs".equals(nodeTopElements.getTagName())) {
                        NodeList inputNodesList = nodeTopElements.getChildNodes();
                        InputNode inputNode = (InputNode) importedNode;
                        for (Integer inputsCount = 0; inputsCount < inputNodesList.getLength(); inputsCount++) {
                            if (inputNodesList.item(inputsCount).getNodeType() == Node.ELEMENT_NODE) {
                                Element inputElement = (Element) inputNodesList.item(inputsCount);

                                String inputVariableName = getTextValue("", inputElement, "VariableName");
                                String inputVariableValue = getTextValue("", inputElement, "VariableValue");

                                Input input = Input.create(Input.class);

                                input.setVariableName(inputVariableName);
                                input.setVariableValue(inputVariableValue);
                                input.setParent(inputNode);
                                input.save();
                                inputNode.addInput(input);
                            }
                        }
                    } else if ("Triggers".equals(nodeTopElements.getTagName())) {
                        NodeList triggersNodeList = nodeTopElements.getChildNodes();
                        TriggerNode triggerNode = (TriggerNode) importedNode;
                        for (Integer triggerCounts = 0; triggerCounts < triggersNodeList.getLength(); triggerCounts++) {
                            if (triggersNodeList.item(triggerCounts).getNodeType() == Node.ELEMENT_NODE) {
                                Element triggerElement = (Element) triggersNodeList.item(triggerCounts);

                                String triggerWatch = getTextValue("", triggerElement, "Watch");
                                String triggerWhen = getTextValue("", triggerElement, "When");
                                String triggerThen = getTextValue("", triggerElement, "Then");

                                Trigger trigger = Trigger.create(Trigger.class);
                                trigger.setThen(triggerThen);
                                trigger.setWatch(triggerWatch);
                                trigger.setWhen(triggerWhen);
                                trigger.setParent(triggerNode);
                                trigger.save();
                                triggerNode.addTrigger(trigger);
                            }
                        }
                    } else if ("Switches".equals(nodeTopElements.getTagName())) {
                        NodeList switchesNodeList = nodeTopElements.getChildNodes();
                        SwitchNode switchNode = (SwitchNode) importedNode;
                        for (Integer switchCount = 0; switchCount < switchesNodeList.getLength(); switchCount++) {
                            if (switchesNodeList.item(switchCount).getNodeType() == Node.ELEMENT_NODE) {
                                Element switchElement = (Element) switchesNodeList.item(switchCount);

                                String switchTarget = getTextValue("", switchElement, "Target");
                                Boolean switchEnabled = Boolean.parseBoolean(getTextValue("", switchElement, "Enabled"));

                                Switch aSwitch = Switch.create(Switch.class);
                                aSwitch.setTarget(switchTarget);
                                aSwitch.setEnabled(switchEnabled);
                                aSwitch.setParent(switchNode);
                                aSwitch.save();
                                switchNode.addSwitch(aSwitch);
                            }
                        }
                    } else if ("DataTableData".equals(nodeTopElements.getTagName())) {
                        NodeList dataTableDataRowList = nodeTopElements.getChildNodes();
                        DataTableNode dataTableNode = (DataTableNode) importedNode;
                        for (Integer rowCount = 0; rowCount < dataTableDataRowList.getLength(); rowCount++) {
                            if (dataTableDataRowList.item(rowCount).getNodeType() == Node.ELEMENT_NODE) {
                                DataTableRow dataTableRow = DataTableRow.create(DataTableRow.class);
                                dataTableRow.setParent(dataTableNode);
                                dataTableRow.save();

                                Element dataTableRowElement = (Element) dataTableDataRowList.item(rowCount);

                                NodeList dataTableDataValueList = dataTableRowElement.getChildNodes();
                                for (Integer valueCount = 0; valueCount < dataTableDataValueList.getLength(); valueCount++) {
                                    if (dataTableDataValueList.item(valueCount).getNodeType() == Node.ELEMENT_NODE) {
                                        Element dataTableValueElement = (Element) dataTableDataValueList.item(valueCount);

                                        String dataKey = getTextValue("", dataTableValueElement, "DataKey");
                                        String dataValue = getTextValue("", dataTableValueElement, "DataValue");
                                        String dataOrder = getTextValue("", dataTableValueElement, "DataOrder");

                                        DataTableValue dataTableValue = DataTableValue.create(DataTableValue.class);
                                        dataTableValue.setParentRow(dataTableRow);
                                        dataTableValue.setDataKey(dataKey);
                                        dataTableValue.setDataValue(dataValue);
                                        dataTableValue.setOrder(Integer.parseInt(dataOrder));
                                        dataTableValue.save();
                                        dataTableRow.addDataTableValue(dataTableValue);
                                    }
                                }

                                dataTableNode.addDataTableRow(dataTableRow);
                            }
                        }
                    }
                }
            }

            importedNode.save();
        }

        DatabaseTransactionManager.getInstance().finaliseTransactions();
    }
}
