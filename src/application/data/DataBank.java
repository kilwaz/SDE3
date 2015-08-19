package application.data;

import application.gui.FlowController;
import application.gui.Program;
import application.node.design.DrawableNode;
import application.node.implementations.CustomObjectNode;
import application.node.implementations.InputNode;
import application.node.implementations.SwitchNode;
import application.node.implementations.TriggerNode;
import application.node.objects.Input;
import application.node.objects.Switch;
import application.node.objects.Trigger;
import application.test.TestResult;
import application.test.TestStep;
import application.utils.CustomObject;
import application.utils.Serializer;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DataBank {
    static public Program currentlyEditProgram;
    static private HashMap<Integer, Program> programs = new HashMap<>();
    static private HashMap<String, HashMap<String, Object>> programVariables = new HashMap<>();
    static private HashMap<String, HashMap<String, Object>> programInstances = new HashMap<>();
    static private HashMap<String, HashMap<String, Object>> testResultInstances = new HashMap<>();
    static private NodeColours nodeColours = new NodeColours();

    static public User currentUser;
    private static Logger log = Logger.getLogger(DataBank.class);

    static public List<String> getProgramNames() {
        return programs.values().stream().map(Program::getName).collect(Collectors.toList());
    }

    public static Program getProgramById(Integer id) {
        return programs.get(id);
    }

    private static void removeProgram(Program program) {
        programs.remove(program.getId());
    }

    private static void addProgram(Program program) {
        programs.put(program.getId(), program);
    }

    static public ArrayList<Program> getPrograms() {
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

    public static void setApplicationUser(String username) {
        currentUser = loadUser(username);
    }

    public static void loadFromDatabase() {
        User user = loadUser("alex");
        currentUser = user;
        loadPrograms(user);
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
                if (DatabaseConnectionWatcher.getInstance().getConnected()) {
                    DatabaseConnectionWatcher.getInstance().setConnected(false);
                }
            }
        } catch (SQLException ex) {
            if (!dbConnection.isConnected()) { // If we are not connected anymore, report this to the user status bar
                DatabaseConnectionWatcher.getInstance().setConnected(false);
            }

            log.error(ex);
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
                if (DatabaseConnectionWatcher.getInstance().getConnected()) {
                    DatabaseConnectionWatcher.getInstance().setConnected(false);
                }
            }
        } catch (SQLException ex) {
            if (!dbConnection.isConnected()) { // If we are not connected anymore, report this to the user status bar
                DatabaseConnectionWatcher.getInstance().setConnected(false);
            }

            log.error(ex);
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
                }

                valueCount++;
            }
        }
    }

    public static void saveUser(User user) {
        Integer currentProgramId = -1;
        if (user.getCurrentProgram() != null) {
            currentProgramId = user.getCurrentProgram().getId();
        }

        new UpdateQuery("update user set last_program = ? where id = ?")
                .addParameter(currentProgramId) // 1
                .addParameter(user.getId()) // 2
                .execute();
    }

    public static void saveProgram(Program program) {
        new UpdateQuery("update program set name = ?, start_node = ?, view_offset_width = ?, view_offset_height = ? where id = ?")
                .addParameter(program.getName()) // 1
                .addParameter(program.getFlowController().getStartNode() == null ? -1 : program.getFlowController().getStartNode().getId()) // 2
                .addParameter(program.getFlowController().getViewOffsetWidth()) // 3
                .addParameter(program.getFlowController().getViewOffsetHeight()) // 4
                .addParameter(program.getId()) // 5
                .execute();
    }

    public static Program createNewProgram(String programName) {
        Program newProgram = new Program(programName);
        newProgram.setId(getNextId("program"));
        addProgram(newProgram);

        new UpdateQuery("insert into program values (default, ?, -1, 0, 0, ?)")
                .addParameter(programName) // 1
                .addParameter(currentUser.getId()) // 2
                .execute();

        return newProgram;
    }

    public static void deleteProgram(Program program) {
        removeProgram(program);

        new UpdateQuery("delete from program where id = ?")
                .addParameter(program.getId()) // 1
                .execute();
    }

    public static void deleteNode(DrawableNode node) {
        new UpdateQuery("delete from node where id = ?")
                .addParameter(node.getId()) // 1
                .execute();
    }

    public static void deleteSwitch(Switch deleteSwitch) {
        new UpdateQuery("delete from switch where id = ?")
                .addParameter(deleteSwitch.getId()) // 1
                .execute();
    }

    public static void deleteInput(Input deleteInput) {
        new UpdateQuery("delete from input where id = ?")
                .addParameter(deleteInput.getId()) // 1
                .execute();
    }

    public static void deleteCustomObject(CustomObject customObject) {
        new UpdateQuery("delete from serialized where id = ?")
                .addParameter(customObject.getId()) // 1
                .execute();
    }

    public static void saveNode(DrawableNode node) {
        UpdateResult updateResult = (UpdateResult) new UpdateQuery("update node set program_id = ?, node_type = ? where id = ?")
                .addParameter(node.getProgramId()) // 1
                .addParameter(node.getNodeType()) // 2
                .addParameter(node.getId()) // 3
                .execute();
        if (updateResult.getResultNumber() == 0) { // If record does not exist insert a new one..
            node.setId(getNextId("node")); // Gets the next ID for a node that is about to be created
            new UpdateQuery("insert into node values (default, ?, ?)")
                    .addParameter(node.getProgramId()) // 1
                    .addParameter(node.getNodeType()) // 2
                    .execute();
        }

        // update node_details
        List<SavableAttribute> savableAttributes = node.getDataToSave();
        for (SavableAttribute savableAttribute : savableAttributes) {
            UpdateResult updateResultNodeDetails = (UpdateResult) new UpdateQuery("update node_details set object_value = ? where node_id = ? and object_name = ? and object_class = ?")
                    .addParameter(savableAttribute.getVariable()) // 1
                    .addParameter(node.getId()) // 2
                    .addParameter(savableAttribute.getVariableName()) // 3
                    .addParameter(savableAttribute.getClassName()) // 4
                    .execute();
            if (updateResultNodeDetails.getResultNumber() == 0) { // If record does not exist insert a new one..
                new UpdateQuery("insert into node_details values (default, ?, ?, ?, ?)")
                        .addParameter(node.getId()) // 1
                        .addParameter(savableAttribute.getVariableName()) // 2
                        .addParameter(savableAttribute.getVariable()) // 3
                        .addParameter(savableAttribute.getClassName()) // 4
                        .execute();
            }
        }
    }

    public static User loadUser(String username) {
        User user = null;

        SelectResult selectResult = (SelectResult) new SelectQuery("select id, username, last_program from user where username = ?")
                .addParameter(username)
                .execute();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            user = new User(resultRow.getInt("id"), resultRow.getString("username"), resultRow.getInt("last_program"));
        }

        return user;
    }

    public static void loadPrograms(User user) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select id,name,start_node,view_offset_height,view_offset_width from program where user_id = ?;")
                .addParameter(user.getId())
                .execute();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            String name = resultRow.getString("name");
            Integer programId = resultRow.getInt("id");
            Integer startNode = resultRow.getInt("start_node");
            Program loadedProgram = new Program(name, programId);

            if (loadedProgram.getId().equals(user.getLastProgram())) {
                user.setCurrentProgram(loadedProgram);
            }

            FlowController flowController = loadedProgram.getFlowController();
            flowController.setViewOffsetHeight(resultRow.getDouble("view_offset_height"));
            flowController.setViewOffsetWidth(resultRow.getDouble("view_offset_width"));

            SelectResult selectResultNode = (SelectResult) new SelectQuery("select id,program_id,node_type from node where program_id = ?;")
                    .addParameter(programId)
                    .execute();
            for (SelectResultRow resultRowNode : selectResultNode.getResults()) {
                DrawableNode drawableNode = flowController.createNewNode(
                        resultRowNode.getInt("id"),
                        resultRowNode.getInt("program_id"),
                        resultRowNode.getString("node_type"),
                        true
                );

                if (drawableNode != null) {
                    // This disables auto saving when running setters.  We don't want that while we are first populating the object
                    drawableNode.setIsInitialising(true);

                    SelectResult selectResultNodeDetail = (SelectResult) new SelectQuery("select node_id,object_name,object_class,object_value from node_details where node_id = ?")
                            .addParameter(resultRowNode.getInt("id"))
                            .execute();
                    for (SelectResultRow resultRowNodeDetail : selectResultNodeDetail.getResults()) {
                        Method method;
                        try {
                            if ("java.lang.Double".equals(resultRowNodeDetail.getString("object_class"))) {
                                Double doubleValue = resultRowNodeDetail.getBlobDouble("object_value");

                                method = drawableNode.getClass().getMethod("set" + resultRowNodeDetail.getString("object_name"), Class.forName(resultRowNodeDetail.getString("object_class")));
                                method.invoke(drawableNode, doubleValue);
                            } else if ("java.lang.String".equals(resultRowNodeDetail.getString("object_class"))) {
                                String stringValue = resultRowNodeDetail.getBlobString("object_value");

                                method = drawableNode.getClass().getMethod("set" + resultRowNodeDetail.getString("object_name"), Class.forName(resultRowNodeDetail.getString("object_class")));
                                method.invoke(drawableNode, stringValue);
                            } else if ("java.lang.Integer".equals(resultRowNodeDetail.getString("object_class"))) {
                                Integer integerValue = resultRowNodeDetail.getBlobInt("object_value");

                                method = drawableNode.getClass().getMethod("set" + resultRowNodeDetail.getString("object_name"), Class.forName(resultRowNodeDetail.getString("object_class")));
                                method.invoke(drawableNode, integerValue);
                            }
                        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException ex) {
                            log.error(ex);
                        }
                    }

                    drawableNode.setIsInitialising(false);
                }
            }

            flowController.setStartNode(flowController.getNodeById(startNode));
            addProgram(loadedProgram);
        }
    }

    public static void loadSwitches(SwitchNode switchNode) {
        List<Switch> aSwitches = new ArrayList<>();

        SelectResult selectResult = (SelectResult) new SelectQuery("select id, target, enabled from switch where node_id = ?")
                .addParameter(switchNode.getId())
                .execute();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            aSwitches.add(new Switch(resultRow.getInt("id"), switchNode, resultRow.getString("target"), resultRow.getBoolean("enabled")));
        }

        switchNode.setSwitches(aSwitches);
    }


    public static void loadInputs(InputNode inputNode) {
        List<Input> inputs = new ArrayList<>();

        SelectResult selectResult = (SelectResult) new SelectQuery("select id, variable_name, variable_value from input where node_id = ?")
                .addParameter(inputNode.getId())
                .execute();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            inputs.add(new Input(resultRow.getInt("id"), resultRow.getString("variable_name"), resultRow.getString("variable_value"), inputNode));
        }

        inputNode.setInputs(inputs);
    }

    public static Switch createNewSwitch(String target, Boolean enabled, SwitchNode parent) {
        Switch aSwitch = new Switch(getNextId("switch"), parent, target, enabled);

        parent.addSwitch(aSwitch);
        DataBank.saveSwitch(aSwitch);

        return aSwitch;
    }

    public static Input createNewInput(String variableName, String variableValue, InputNode parent) {
        Input input = new Input(getNextId("input"), variableName, variableValue, parent);

        parent.addInput(input);
        DataBank.saveInput(input);

        return input;
    }

    public static void loadTriggers(TriggerNode triggerNode) {
        List<Trigger> triggers = new ArrayList<>();

        SelectResult selectResult = (SelectResult) new SelectQuery("select id, trigger_watch, trigger_when, trigger_then from trigger_condition where node_id = ?")
                .addParameter(triggerNode.getId())
                .execute();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            triggers.add(new Trigger(resultRow.getInt("id"), resultRow.getString("trigger_watch"), resultRow.getString("trigger_when"), resultRow.getString("trigger_then"), triggerNode));
        }

        triggerNode.setTriggers(triggers);
    }

    public static void saveSwitch(Switch aSwitch) {
        UpdateResult updateResult = (UpdateResult) new UpdateQuery("update switch set target = ?, enabled = ? where id = ?")
                .addParameter(aSwitch.getTarget()) // 1
                .addParameter(aSwitch.isEnabled()) // 2
                .addParameter(aSwitch.getId()) // 3
                .execute();
        if (updateResult.getResultNumber() == 0) { // If record does not exist insert a new one..
            aSwitch.setId(getNextId("switch"));
            new UpdateQuery("insert into switch values (default, ?, ?, ?)")
                    .addParameter(aSwitch.getParent().getId()) // 1
                    .addParameter(aSwitch.getTarget()) // 2
                    .addParameter(aSwitch.isEnabled()) // 3
                    .execute();
        }
    }

    public static void saveInput(Input input) {
        UpdateResult updateResult = (UpdateResult) new UpdateQuery("update input set variable_name = ?, variable_value = ? where id = ?")
                .addParameter(input.getVariableName()) // 1
                .addParameter(input.getVariableValue()) // 2
                .addParameter(input.getId()) // 3
                .execute();
        if (updateResult.getResultNumber() == 0) { // If record does not exist insert a new one..
            input.setId(getNextId("input"));
            new UpdateQuery("insert into input values (default, ?, ?, ?)")
                    .addParameter(input.getParent().getId()) // 1
                    .addParameter(input.getVariableName()) // 2
                    .addParameter(input.getVariableValue()) // 3
                    .execute();
        }
    }

    public static void saveTrigger(Trigger trigger) {
        UpdateResult updateResult = (UpdateResult) new UpdateQuery("update trigger_condition set trigger_watch = ?, trigger_when = ?, trigger_then = ? where id = ?")
                .addParameter(trigger.getWatch()) // 1
                .addParameter(trigger.getWhen()) // 2
                .addParameter(trigger.getThen()) // 3
                .addParameter(trigger.getId()) // 4
                .execute();
        if (updateResult.getResultNumber() == 0) { // If record does not exist insert a new one..
            trigger.setId(getNextId("trigger_condition"));
            new UpdateQuery("insert into trigger_condition values (default, ?, ?, ?, ?)")
                    .addParameter(trigger.getParent().getId()) // 1
                    .addParameter(trigger.getWatch()) // 2
                    .addParameter(trigger.getWhen()) // 3
                    .addParameter(trigger.getThen()) // 4
                    .execute();
        }
    }

    public static void saveNodeColour(NodeColour nodeColour) {
        UpdateResult updateResult = (UpdateResult) new UpdateQuery("update node_colour set colour_r = ?, colour_g = ?, colour_b = ? where node_type = ?")
                .addParameter(nodeColour.getRed()) // 1
                .addParameter(nodeColour.getGreen()) // 2
                .addParameter(nodeColour.getBlue()) // 3
                .addParameter(nodeColour.getNodeType()) // 4
                .execute();
        if (updateResult.getResultNumber() == 0) { // If record does not exist insert a new one..
            nodeColour.setId(getNextId("node_colour"));
            new UpdateQuery("insert into node_colour values (default, ?, ?, ?, ?)")
                    .addParameter(nodeColour.getNodeType()) // 1
                    .addParameter(nodeColour.getRed()) // 2
                    .addParameter(nodeColour.getGreen()) // 3
                    .addParameter(nodeColour.getBlue()) // 4
                    .execute();
        }
    }

    public static Trigger createNewTrigger(String watch, String when, String then, TriggerNode parent) {
        Trigger trigger = new Trigger(getNextId("trigger_condition"), watch, when, then, parent);

        parent.addTrigger(trigger);
        DataBank.saveTrigger(trigger);

        return trigger;
    }

    public static TestResult createNewTestResult() {
        TestResult newTestResult = new TestResult();
        newTestResult.setId(getNextId("test_result"));

        new UpdateQuery("insert into test_result values (default)")
                .execute();

        return newTestResult;
    }

    public static TestStep createNewTestStep(TestResult testResult) {
        TestStep newTestStep = new TestStep();
        newTestStep.setId(getNextId("test_step"));
        newTestStep.setParentResult(testResult);

        new UpdateQuery("insert into test_step values (default,NULL,NULL,NULL,NULL,NULL,NULL,NULL)")
                .execute();

        return newTestStep;
    }

    public static void saveTestStep(TestStep testStep) {
        UpdateResult updateResult = (UpdateResult) new UpdateQuery("update test_step set test_string = ?, expected_equal = ?, observed_equal = ?, screenshot = ?, successful = ?, test_result = ?, test_type = ? where id = ?")
                .addParameter(testStep.getTestString()) // 1
                .addParameter(testStep.getExpectedEqual()) // 2
                .addParameter(testStep.getObservedEqual()) // 3
                .addParameter(testStep.getScreenshotInputStream()) // 4
                .addParameter(testStep.getSuccessful()) // 5
                .addParameter(testStep.getParentResult().getId()) // 6
                .addParameter(testStep.getTestType()) // 7
                .addParameter(testStep.getId()) // 8
                .execute();
        if (updateResult.getResultNumber() == 0) { // If record does not exist insert a new one..
            testStep.setId(getNextId("test_step"));
            new UpdateQuery("insert into test_step values (default, ?, ?, ?, ?, ?, ?, ?)")
                    .addParameter(testStep.getTestString()) // 1
                    .addParameter(testStep.getExpectedEqual()) // 2
                    .addParameter(testStep.getObservedEqual()) // 3
                    .addParameter(testStep.getScreenshotInputStream()) // 4
                    .addParameter(testStep.getSuccessful()) // 5
                    .addParameter(testStep.getParentResult().getId()) // 6
                    .addParameter(testStep.getTestType()) // 7
                    .execute();
        }
    }

    public static void loadTestSteps(TestResult testResult) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select id, test_string, successful, screenshot, test_result from test_step where test_result = ?")
                .addParameter(testResult.getId())
                .execute();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            try {
                testResult.addTestStep(new TestStep(resultRow.getInt("id"),
                        resultRow.getString("test_string"),
                        resultRow.getBoolean("successful"),
                        ImageIO.read(resultRow.getBlobInputStream("screenshot")),
                        testResult));
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    public static void loadNodeColours(NodeColours nodeColours) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select id, colour_r, colour_g, colour_b, node_type from node_colour")
                .execute();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            NodeColour nodeColour = new NodeColour(resultRow.getInt("colour_r"),
                    resultRow.getInt("colour_g"),
                    resultRow.getInt("colour_b"),
                    resultRow.getString("node_type"));

            nodeColours.addNodeColour(nodeColour);
        }
    }

    private static Integer getNextId(String tableName) {
        Integer autoIncrement = -1;

        SelectResult selectResult = (SelectResult) new SelectQuery("SELECT AUTO_INCREMENT FROM information_schema.tables WHERE table_name = ? AND table_schema = DATABASE();")
                .addParameter(tableName)
                .execute();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            autoIncrement = resultRow.getBigInt("AUTO_INCREMENT").intValue();
        }

        return autoIncrement;
    }

    public static CustomObject createNewCustomObject(Object payload, String payLoadReference, CustomObjectNode parent) {
        CustomObject customObject = new CustomObject(getNextId("serialized"), payload, payLoadReference, parent);

        parent.addCustomObject(customObject);
        DataBank.saveCustomObject(customObject);

        return customObject;
    }

    public static void saveCustomObject(CustomObject customObject) {
        UpdateResult updateResult = (UpdateResult) new UpdateQuery("update serialized set node_id = ?, serial_object = ?, serial_reference = ? where id = ?")
                .addParameter(customObject.getParentNode().getId()) // 1
                .addParameter(Serializer.serializeToInputStream(customObject.getPayload())) // 2
                .addParameter(customObject.getPayLoadReference()) // 3
                .addParameter(customObject.getId()) // 4
                .execute();
        if (updateResult.getResultNumber() == 0) { // If record does not exist insert a new one..
            customObject.setId(getNextId("serialized"));
            new UpdateQuery("insert into serialized values (default, ?, ?, ?)")
                    .addParameter(customObject.getParentNode().getId()) // 1
                    .addParameter(Serializer.serializeToInputStream(customObject.getPayload())) // 2
                    .addParameter(customObject.getPayLoadReference()) // 3
                    .execute();
        }
    }


    public static void loadCustomObjects(CustomObjectNode customObjectNode) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select id, serial_object, serial_reference from serialized where node_id = ?")
                .addParameter(customObjectNode.getId()) // 1
                .execute();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            CustomObject customObject = new CustomObject(resultRow.getInt("id"),
                    Serializer.deserialize(resultRow.getBlobInputStream("serial_object")),
                    resultRow.getString("serial_reference"),
                    customObjectNode
            );

            customObjectNode.addCustomObject(customObject);
        }
    }

    public static NodeColours getNodeColours() {
        return nodeColours;
    }
}