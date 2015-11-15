package application.data;

import application.data.model.dao.UserDAO;
import application.error.Error;
import application.gui.Program;
import application.net.proxy.RecordedHeader;
import application.net.proxy.RecordedProxy;
import application.net.proxy.RecordedRequest;
import application.node.implementations.*;
import application.node.objects.Input;
import application.node.objects.SDEFile;
import application.node.objects.Switch;
import application.node.objects.Trigger;
import application.node.objects.datatable.DataTableRow;
import application.node.objects.datatable.DataTableValue;
import application.test.TestResult;
import application.test.TestStep;
import application.utils.AppParams;
import application.utils.CustomObject;
import application.utils.managers.SessionManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DataBank {
    public static Program currentlyEditProgram;
    private static HashMap<String, Program> programs = new HashMap<>();
    private static HashMap<String, HashMap<String, Object>> programVariables = new HashMap<>();
    private static HashMap<String, HashMap<String, Object>> programInstances = new HashMap<>();
    private static HashMap<String, HashMap<String, Object>> testResultInstances = new HashMap<>();
    private static NodeColours nodeColours = new NodeColours();

    private static Logger log = Logger.getLogger(DataBank.class);

    public static List<String> getProgramNames() {
        return programs.values().stream().map(Program::getName).collect(Collectors.toList());
    }

    public static Program getProgramById(Integer id) {
        return programs.get(id);
    }

    public static ArrayList<Program> getPrograms() {
        return new ArrayList<>(programs.values());
    }

    public static void saveVariable(String name, Object object, String referenceID) {
        HashMap<String, Object> programVariable = programVariables.get(referenceID);
        if (programVariable == null) {
            programVariable = new HashMap<>();
        }
        programVariable.put(name, object);
        programVariables.put(referenceID, programVariable);
    }

    public static Object loadVariable(String name, String referenceID) {
        HashMap<String, Object> programVariable = programVariables.get(referenceID);
        if (programVariable != null) {
            return programVariable.get(name);
        }
        return null;
    }

    public static void resetInstanceObject(String referenceID) {
        programInstances.remove(referenceID);
    }

    public static void saveInstanceObject(String referenceID, String name, Object instance) {
        HashMap<String, Object> instances = programInstances.get(referenceID);
        if (instances == null) {
            instances = new HashMap<>();
        }

        instances.put(name, instance);
        programInstances.put(referenceID, instances);
    }

    public static Object getInstanceObject(String referenceID, String name) {
        HashMap<String, Object> instances = programInstances.get(referenceID);
        if (instances != null) {
            return instances.get(name);
        }

        return null;
    }

    public static void createCurrentSession() {
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserFromUsername(AppParams.CURRENT_USER);
        Session session = new Session();
        session.setUser(user);
        SessionManager.getInstance().addSession(session);
    }

    public static SelectResult runSelectQuery(SelectQuery selectQuery) {
        return runSelectQuery(DBConnectionManager.getInstance().getApplicationConnection(), selectQuery);
    }

    public static SelectResult runSelectQuery(DBConnection dbConnection, SelectQuery selectQuery) {
        SelectResult selectResult = new SelectResult();
        try {
            if (dbConnection.isConnected()) {
                PreparedStatement preparedStatement = dbConnection.getPreparedStatement(selectQuery.getQuery());
                if (preparedStatement != null) {
                    setParameters(preparedStatement, selectQuery);

                    ResultSet resultSet = preparedStatement.executeQuery();

                    while (resultSet.next()) {
                        SelectResultRow selectResultRow = new SelectResultRow();

                        for (int i = 1; i < resultSet.getMetaData().getColumnCount() + 1; i++) {
                            if (resultSet.getMetaData().getColumnType(i) == -4) {  // Column type '-4' is BLOB
                                // '-Blob' is appended to the end of the result to show that it is the blob representation of the object out of the database
                                selectResultRow.addColumn(resultSet.getMetaData().getColumnName(i) + "-Blob", resultSet.getBlob(i));
                                // '-String' is appended to the end of the result to show that it is the string representation of the object out of the database
                                selectResultRow.addColumn(resultSet.getMetaData().getColumnName(i) + "-String", resultSet.getString(i));
                                selectResultRow.addColumn(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
                            } else {
                                selectResultRow.addColumn(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
                            }
                        }

                        selectResult.addResultRow(selectResultRow);
                    }
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } else {
                if (dbConnection.isApplicationConnection() && dbConnection.isApplicationConnection()) {
                    DatabaseConnectionWatcher.getInstance().setConnected(false);
                }
            }
        } catch (SQLException ex) {
            if (!dbConnection.isConnected() && dbConnection.isApplicationConnection()) { // If we are not connected anymore, report this to the user status bar
                DatabaseConnectionWatcher.getInstance().setConnected(false);
            }

            Error.SELECT_QUERY.record().additionalInformation(selectQuery.getQuery()).create(ex);
        }

        return selectResult;
    }

    public static UpdateResult runUpdateQuery(UpdateQuery updateQuery) {
        return runUpdateQuery(DBConnectionManager.getInstance().getApplicationConnection(), updateQuery);
    }

    public static UpdateResult runUpdateQuery(DBConnection dbConnection, UpdateQuery updateQuery) {
        UpdateResult updateResult = new UpdateResult();
        try {
            if (dbConnection.isConnected()) {
                PreparedStatement preparedStatement = dbConnection.getPreparedStatement(updateQuery.getQuery());
                if (preparedStatement != null) {
                    setParameters(preparedStatement, updateQuery);
                    updateResult.setResultNumber(preparedStatement.executeUpdate());
                    preparedStatement.close();
                }
            } else {
                if (dbConnection.isApplicationConnection()) {
                    DatabaseConnectionWatcher.getInstance().setConnected(false);
                    // Reconnect to the DB
                    log.info("Trying to reconnect after seeing  the exception");
                    dbConnection.connect();
                }
            }
        } catch (SQLException ex) {
            StringBuilder params = new StringBuilder();

            params.append("Parameters:\n");

            Integer paramCount = 1;
            for (Object param : updateQuery.getParameters()) {
                params.append("\n\t\t").append(paramCount).append(": ").append(param);
                paramCount++;
            }

            Error.UPDATE_QUERY.record().additionalInformation(updateQuery.getQuery()).additionalInformation(params.toString()).create(ex);
            if (!dbConnection.isConnected() && dbConnection.isApplicationConnection()) { // If we are not connected anymore, report this to the user status bar
                DatabaseConnectionWatcher.getInstance().setConnected(false);
                log.info("Trying to reconnect via the exception");
                dbConnection.connect();
            }
        }

        return updateResult;
    }

    private static void setParameters(PreparedStatement preparedStatement, Query query) throws SQLException {
        if (preparedStatement != null) {
            Integer valueCount = 1;
            for (Object value : query.getParameters()) {
                if (value instanceof Boolean) {
                    preparedStatement.setBoolean(valueCount, (Boolean) value);
                } else if (value instanceof Double) {
                    preparedStatement.setDouble(valueCount, (Double) value);
                } else if (value instanceof InputStream) {
                    preparedStatement.setBlob(valueCount, (InputStream) value);
                } else if (value instanceof Integer) {
                    preparedStatement.setInt(valueCount, (Integer) value);
                } else if (value instanceof String) {
                    preparedStatement.setString(valueCount, (String) value);
                } else if (value instanceof UUID) {
                    preparedStatement.setString(valueCount, value.toString());
                } else if (value == null) {
                    preparedStatement.setObject(valueCount, null);
                }

                valueCount++;
            }
        }
    }

    public static void loadPrograms(User user) {
//        SelectResult selectResult = (SelectResult) new SelectQuery("select id,name,start_node,view_offset_height,view_offset_width from program where user_id = ?;")
//                .addParameter(user.getId())
//                .execute();
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            String name = resultRow.getString("name");
//            Integer programId = resultRow.getInt("id");
//            Integer startNode = resultRow.getInt("start_node");
//            Program loadedProgram = new Program(name, programId);
//
//            if (loadedProgram.getId().equals(user.getLastProgram())) {
//                user.setCurrentProgram(loadedProgram);
//            }
//
//            FlowController flowController = loadedProgram.getFlowController();
//            flowController.setViewOffsetHeight(resultRow.getDouble("view_offset_height"));
//            flowController.setViewOffsetWidth(resultRow.getDouble("view_offset_width"));
//
//            SelectResult selectResultNode = (SelectResult) new SelectQuery("select id,program_id,node_type from node where program_id = ?;")
//                    .addParameter(programId)
//                    .execute();
//            for (SelectResultRow resultRowNode : selectResultNode.getResults()) {
//                DrawableNode drawableNode = flowController.createNewNode(
//                        resultRowNode.getInt("id"),
//                        resultRowNode.getInt("program_id"),
//                        resultRowNode.getString("node_type"),
//                        true
//                );
//
//                if (drawableNode != null) {
//                    // This disables auto saving when running setters.  We don't want that while we are first populating the object
//                    drawableNode.setIsInitialising(true);
//
//                    SelectResult selectResultNodeDetail = (SelectResult) new SelectQuery("select node_id,object_name,object_class,object_value from node_details where node_id = ?")
//                            .addParameter(resultRowNode.getInt("id"))
//                            .execute();
//                    for (SelectResultRow resultRowNodeDetail : selectResultNodeDetail.getResults()) {
//                        Method method;
//                        try {
//                            if ("java.lang.Double".equals(resultRowNodeDetail.getString("object_class"))) {
//                                Double doubleValue = resultRowNodeDetail.getBlobDouble("object_value");
//
//                                method = drawableNode.getClass().getMethod("set" + resultRowNodeDetail.getString("object_name"), Class.forName(resultRowNodeDetail.getString("object_class")));
//                                method.invoke(drawableNode, doubleValue);
//                            } else if ("java.lang.String".equals(resultRowNodeDetail.getString("object_class"))) {
//                                String stringValue = resultRowNodeDetail.getBlobString("object_value");
//
//                                method = drawableNode.getClass().getMethod("set" + resultRowNodeDetail.getString("object_name"), Class.forName(resultRowNodeDetail.getString("object_class")));
//                                method.invoke(drawableNode, stringValue);
//                            } else if ("java.lang.Integer".equals(resultRowNodeDetail.getString("object_class"))) {
//                                Integer integerValue = resultRowNodeDetail.getBlobInt("object_value");
//
//                                method = drawableNode.getClass().getMethod("set" + resultRowNodeDetail.getString("object_name"), Class.forName(resultRowNodeDetail.getString("object_class")));
//                                method.invoke(drawableNode, integerValue);
//                            }
//                        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException ex) {
//                            Error.LOADING_PROGRAM.record().create(ex);
//                        }
//                    }
//
//                    drawableNode.setIsInitialising(false);
//                }
//            }
//
//            flowController.setStartNode(flowController.getNodeById(startNode));
//            addProgram(loadedProgram);
//        }
    }

    public static void loadSwitches(SwitchNode switchNode) {
//        List<Switch> aSwitches = new ArrayList<>();
//
//        SelectResult selectResult = (SelectResult) new SelectQuery("select id, target, enabled from switch where node_id = ?")
//                .addParameter(switchNode.getId())
//                .execute();
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            aSwitches.add(new Switch(resultRow.getInt("id"), switchNode, resultRow.getString("target"), resultRow.getBoolean("enabled")));
//        }
//
//        switchNode.setSwitches(aSwitches);
    }


    public static void loadInputs(InputNode inputNode) {
//        List<Input> inputs = new ArrayList<>();
//
//        SelectResult selectResult = (SelectResult) new SelectQuery("select id, variable_name, variable_value from input where node_id = ?")
//                .addParameter(inputNode.getId())
//                .execute();
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            inputs.add(new Input(resultRow.getInt("id"), resultRow.getString("variable_name"), resultRow.getString("variable_value"), inputNode));
//        }
//
//        inputNode.setInputs(inputs);
    }

    public static Switch createNewSwitch(String target, Boolean enabled, SwitchNode parent) {
//        Switch aSwitch = new Switch(getNextId("switch"), parent, target, enabled);
//
//        parent.addSwitch(aSwitch);
//        aSwitch.save();
//
//        return aSwitch;
        return null;
    }

    public static Input createNewInput(String variableName, String variableValue, InputNode parent) {
//        Input input = new Input(getNextId("input"), variableName, variableValue, parent);
//
//        parent.addInput(input);
//        input.save();
//
//        return input;
        return null;
    }

    public static void loadTriggers(TriggerNode triggerNode) {
//        List<Trigger> triggers = new ArrayList<>();
//
//        SelectResult selectResult = (SelectResult) new SelectQuery("select id, trigger_watch, trigger_when, trigger_then from trigger_condition where node_id = ?")
//                .addParameter(triggerNode.getId())
//                .execute();
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            triggers.add(new Trigger(resultRow.getInt("id"), resultRow.getString("trigger_watch"), resultRow.getString("trigger_when"), resultRow.getString("trigger_then"), triggerNode));
//        }
//
//        triggerNode.setTriggers(triggers);
    }

    public static Trigger createNewTrigger(String watch, String when, String then, TriggerNode parent) {
//        Trigger trigger = new Trigger(getNextId("trigger_condition"), watch, when, then, parent);
//
//        parent.addTrigger(trigger);
//        trigger.save();
//
//        return trigger;
        return null;
    }

    public static TestResult createNewTestResult() {
//        TestResult newTestResult = new TestResult();
//        newTestResult.setId(getNextId("test_result"));
//
//        new UpdateQuery("insert into test_result values (default)")
//                .execute();
//
//        return newTestResult;
        return null;
    }

    public static TestStep createNewTestStep(TestResult testResult) {
//        TestStep newTestStep = new TestStep();
//        newTestStep.setId(getNextId("test_step"));
//        newTestStep.setParentResult(testResult);
//
//        new UpdateQuery("insert into test_step values (default,NULL,NULL,NULL,NULL,NULL,NULL,NULL)")
//                .execute();
//
//        return newTestStep;
        return null;
    }

    public static void loadTestSteps(TestResult testResult) {
//        SelectResult selectResult = (SelectResult) new SelectQuery("select id, test_string, successful, screenshot, test_result from test_step where test_result = ?")
//                .addParameter(testResult.getId())
//                .execute();
//
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            try {
//                testResult.addTestStep(new TestStep(resultRow.getInt("id"),
//                        resultRow.getString("test_string"),
//                        resultRow.getBoolean("successful"),
//                        ImageIO.read(resultRow.getBlobInputStream("screenshot")),
//                        testResult));
//            } catch (IOException ex) {
//                Error.LOADING_TEST_STEP.record().create(ex);
//            }
//        }
    }

    public static void loadNodeColours(NodeColours nodeColours) {
//        SelectResult selectResult = (SelectResult) new SelectQuery("select id, colour_r, colour_g, colour_b, node_type from node_colour")
//                .execute();
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            NodeColour nodeColour = new NodeColour(resultRow.getInt("colour_r"),
//                    resultRow.getInt("colour_g"),
//                    resultRow.getInt("colour_b"),
//                    resultRow.getString("node_type"));
//
//            nodeColours.addNodeColour(nodeColour);
//        }
    }

    public static CustomObject createNewCustomObject(Object payload, String payLoadReference, CustomObjectNode parent) {
//        CustomObject customObject = new CustomObject(getNextId("serialized"), payload, payLoadReference, parent);
//
//        parent.addCustomObject(customObject);
//        DataBank.saveCustomObject(customObject);

        return null;
    }

    public static void loadCustomObjects(CustomObjectNode customObjectNode) {
//        SelectResult selectResult = (SelectResult) new SelectQuery("select id, serial_object, serial_reference from serialized where node_id = ?")
//                .addParameter(customObjectNode.getId()) // 1
//                .execute();
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            CustomObject customObject = new CustomObject(resultRow.getInt("id"),
//                    Serializer.deserialize(resultRow.getBlobInputStream("serial_object")),
//                    resultRow.getString("serial_reference"),
//                    customObjectNode
//            );
//
//            customObjectNode.addCustomObject(customObject);
//        }
    }

    public static SDEFile createNewSDEFile(FileStoreNode parent) {
//        SDEFile sdeFile = new SDEFile(getNextId("serialized"), parent);
//
//        parent.addSDEFile(sdeFile);
//        sdeFile.save();
//
        return null;
    }

    public static SDEFile createNewSDEFile(File file, FileStoreNode parent) {
//        SDEFile sdeFile = new SDEFile(getNextId("serialized"), file, parent);
//
//        parent.addSDEFile(sdeFile);
//        sdeFile.save();
//
        return null;
    }

    public static void loadSDEFile(FileStoreNode fileStoreNode) {
//        SelectResult selectResult = (SelectResult) new SelectQuery("select id, serial_object, serial_reference from serialized where node_id = ?")
//                .addParameter(fileStoreNode.getId()) // 1
//                .execute();
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            String userHome = System.getProperty("user.home");
//
//            // Save source in .java file.
//            File root = new File(userHome, "/SDE"); // On Windows running on C:\, this is C:\java.
//            File targetFile = new File(root, "sdeFiles/" + resultRow.getInt("id") + ".tmp");
//            Boolean mkDirResult = targetFile.getParentFile().mkdirs();
//
//            OutputStream outStream = null;
//            try {
//                outStream = new FileOutputStream(targetFile);
//
//                byte[] buffer = new byte[8 * 1024];
//                int bytesRead;
//                InputStream blobInputStream = resultRow.getBlobInputStream("serial_object");
//                while ((bytesRead = blobInputStream.read(buffer)) != -1) {
//                    outStream.write(buffer, 0, bytesRead);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    if (outStream != null) {
//                        outStream.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            SDEFile sdeFile = new SDEFile(resultRow.getInt("id"),
//                    targetFile,
//                    fileStoreNode
//            );
//
//            fileStoreNode.addSDEFile(sdeFile);
//        }
    }

    public static RecordedRequest createNewRecordedRequest(RecordedProxy parent) {
//        RecordedRequest recordedRequest = new RecordedRequest(getNextId("recorded_requests"), parent);
//        recordedRequest.save();
        return null;
    }

    public static RecordedProxy createNewRecordedProxy() {
//        RecordedProxy recordedProxy = new RecordedProxy(getNextId("http_proxys"));
//        recordedProxy.save();
        return null;
    }

    public static DataTableRow createNewDataTableRow(DataTableNode parent) {
//        DataTableRow dataTableRow = new DataTableRow(getNextId("data_table_rows"), parent);
//
//        dataTableRow.save();
//        parent.addDataTableRow(dataTableRow);
//
//        return dataTableRow;
        return null;
    }

    public static RecordedHeader createNewRecordedHeader(RecordedRequest recordedRequest) {
//        RecordedHeader recordedHeader = new RecordedHeader(getNextId("http_headers"), recordedRequest);
//        recordedHeader.save();
//        return recordedHeader;
        return null;
    }

    public static void loadLazyRequest(RecordedRequest recordedRequest) {
//        SelectResult selectResult = (SelectResult) new SelectQuery("select request_content from recorded_requests where id = ?")
//                .addParameter(recordedRequest.getId())
//                .execute();
//
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            recordedRequest.setRequest(resultRow.getString("request_content"));
//        }
    }

    public static void loadLazyResponse(RecordedRequest recordedRequest) {
//        SelectResult selectResult = (SelectResult) new SelectQuery("select response_content from recorded_requests where id = ?")
//                .addParameter(recordedRequest.getId())
//                .execute();
//
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            recordedRequest.setResponse(resultRow.getString("response_content"));
//        }
    }

    public static RecordedRequest loadRecordedRequest(String id, RecordedProxy recordedProxy) {
//        SelectResult selectResult = (SelectResult) new SelectQuery("select id, http_proxy_id, url, duration, request_size, response_size from recorded_requests where id = ?")
//                .addParameter(id) // 1
//                .execute();
//        RecordedRequest recordedRequest = null;
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            recordedRequest = new RecordedRequest(resultRow.getInt("id"), recordedProxy);
//            recordedRequest.setURL(resultRow.getString("url"));
//            recordedRequest.setDuration(resultRow.getInt("duration"));
//            recordedRequest.setRequestSize(resultRow.getInt("request_size"));
//            recordedRequest.setResponseSize(resultRow.getInt("response_size"));
//        }
//        return recordedRequest;
        return null;
    }

    public static void loadRecordedHeaders(RecordedRequest recordedRequest) {
//        SelectResult selectResult = (SelectResult) new SelectQuery("select id, header_name, header_value, header_type from http_headers where request_id = ?")
//                .addParameter(recordedRequest.getId()) // 1
//                .execute();
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            RecordedHeader recordedHeader = new RecordedHeader(resultRow.getInt("id"),
//                    recordedRequest
//            );
//            recordedHeader.setName(resultRow.getString("header_name"));
//            recordedHeader.setValue(resultRow.getString("header_value"));
//            recordedHeader.setType(resultRow.getString("header_type"));
//
//            recordedRequest.addRecordedHeader(recordedHeader);
//        }
    }

    public static void loadDataTableRows(DataTableNode dataTableNode) {
//        SelectResult selectResult = (SelectResult) new SelectQuery("select id, node_id from data_table_rows where node_id = ?")
//                .addParameter(dataTableNode.getId()) // 1
//                .execute();
//        List<DataTableRow> dataTableRows = new ArrayList<>();
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            DataTableRow dataTableRow = new DataTableRow(resultRow.getInt("id"),
//                    dataTableNode
//            );
//
//            dataTableRows.add(dataTableRow);
//        }
//        dataTableNode.addAllDataTableRow(dataTableRows);
    }

    public static DataTableValue createNewDataTableValue(DataTableRow parentRow, String key, String value) {
//        DataTableValue dataTableValue = new DataTableValue(getNextId("data_table_values"), key, value, parentRow);
//
//        parentRow.addDataTableValue(dataTableValue);
//        dataTableValue.save();
//
//        return dataTableValue;
        return null;
    }

    public static void loadDataTableValue(DataTableRow dataTableRow) {
//        SelectResult selectResult = (SelectResult) new SelectQuery("select id, data_table_id, data_key, data_value from data_table_values where data_table_id = ?")
//                .addParameter(dataTableRow.getId()) // 1
//                .execute();
//        for (SelectResultRow resultRow : selectResult.getResults()) {
//            DataTableValue dataTableValue = new DataTableValue(resultRow.getInt("id"),
//                    resultRow.getString("data_key"),
//                    resultRow.getString("data_value"),
//                    dataTableRow
//            );
//
//            dataTableRow.addDataTableValue(dataTableValue);
//        }
    }

    public static NodeColours getNodeColours() {
        return nodeColours;
    }
}