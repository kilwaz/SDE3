package application.data;

import application.gui.FlowController;
import application.gui.Program;
import application.node.design.DrawableNode;
import application.node.implementations.InputNode;
import application.node.implementations.SwitchNode;
import application.node.implementations.TriggerNode;
import application.node.objects.Input;
import application.node.objects.Switch;
import application.node.objects.Trigger;

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
    static private MySQLConnectionManager mySQLInstance;
    static private NodeColours nodeColours = new NodeColours();

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

    public static void loadFromDatabase() {
        loadPrograms();
    }

    public static void saveProgram(Program program) {
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("update program set name = ?, start_node = ?, view_offset_width = ?, view_offset_height = ? where id = ?");
            if (preparedStatement != null) {
                preparedStatement.setString(1, program.getName());
                if (program.getFlowController().getStartNode() == null) { // There might be no start node
                    preparedStatement.setInt(2, -1);
                } else {
                    preparedStatement.setInt(2, program.getFlowController().getStartNode().getId());
                }
                preparedStatement.setDouble(3, program.getFlowController().getViewOffsetWidth());
                preparedStatement.setDouble(4, program.getFlowController().getViewOffsetHeight());
                preparedStatement.setInt(5, program.getId());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Program createNewProgram(String programName) {
        Program newProgram = new Program(programName);
        addProgram(newProgram);
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("insert into program values (default, ?, NULL, NULL, NULL)");
            if (preparedStatement != null) {
                preparedStatement.setString(1, programName);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newProgram;
    }

    public static void deleteProgram(Program program) {
        removeProgram(program);

        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            // Delete all nodes associated with program
            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("delete from node where program_id = ?");
            if (preparedStatement != null) {
                preparedStatement.setInt(1, program.getId());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }

            // Delete program record
            preparedStatement = mySQLInstance.getPreparedStatement("delete from program where id = ?");
            if (preparedStatement != null) {
                preparedStatement.setInt(1, program.getId());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteNode(DrawableNode node) {
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("delete from node where id = ?");
            if (preparedStatement != null) {
                preparedStatement.setInt(1, node.getId());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteSwitch(Switch deleteSwitch) {
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("delete from switch where id = ?");
            if (preparedStatement != null) {
                preparedStatement.setInt(1, deleteSwitch.getId());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveNode(DrawableNode node) {
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("update node set program_id = ?, node_type = ? where id = ?");
            preparedStatement.setInt(1, node.getProgramId());
            preparedStatement.setString(2, node.getNodeType());
            preparedStatement.setInt(3, node.getId());
            int result = preparedStatement.executeUpdate();
            preparedStatement.close();

            if (result == 0) { // If record does not exist insert a new one..
                node.setId(getNextId("node")); // Gets the next ID for a node that is about to be created

                preparedStatement = mySQLInstance.getPreparedStatement("insert into node values (default, ?, ?)");
                if (preparedStatement != null) {
                    preparedStatement.setInt(1, node.getProgramId());
                    preparedStatement.setString(2, node.getNodeType());

                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                }
            }

            // update node_details
            List<SavableAttribute> savableAttributes = node.getDataToSave();
            for (SavableAttribute savableAttribute : savableAttributes) {
                preparedStatement = mySQLInstance.getPreparedStatement("update node_details set object_value = ? where node_id = ? and object_name = ? and object_class = ?");
                if (preparedStatement != null) {
                    preparedStatement.setObject(1, savableAttribute.getVariable());
                    preparedStatement.setInt(2, node.getId());
                    preparedStatement.setString(3, savableAttribute.getVariableName());
                    preparedStatement.setString(4, savableAttribute.getClassName());

                    result = preparedStatement.executeUpdate();
                    preparedStatement.close();

                    if (result == 0) { // If record does not exist insert a new one..
                        //System.out.println(node.getId() + " " + savableAttribute.getVariable() + " does not exist, adding it..");
                        preparedStatement = mySQLInstance.getPreparedStatement("insert into node_details values (default, ?, ?, ?, ?)");
                        if (preparedStatement != null) {
                            preparedStatement.setInt(1, node.getId());
                            preparedStatement.setString(2, savableAttribute.getVariableName());
                            preparedStatement.setObject(3, savableAttribute.getVariable());
                            preparedStatement.setString(4, savableAttribute.getClassName());
                            preparedStatement.executeUpdate();
                            preparedStatement.close();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadPrograms() {
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            ResultSet resultSet = mySQLInstance.runQuery("select id,name,start_node,view_offset_height,view_offset_width from program;");
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                Integer programId = resultSet.getInt("id");
                Integer startNode = resultSet.getInt("start_node");
                Program loadedProgram = new Program(name, programId);

                FlowController flowController = loadedProgram.getFlowController();
                flowController.setViewOffsetHeight(resultSet.getDouble("view_offset_height"));
                flowController.setViewOffsetWidth(resultSet.getDouble("view_offset_width"));

                ResultSet nodeResultSet = mySQLInstance.runQuery("select id,program_id,node_type from node where program_id = '" + programId + "';");

                while (nodeResultSet.next()) {
                    DrawableNode drawableNode = flowController.createNewNode(
                            nodeResultSet.getInt("id"),
                            nodeResultSet.getInt("program_id"),
                            nodeResultSet.getString("node_type"),
                            true
                    );

                    if (drawableNode != null) {
                        // This disables auto saving when running setters.  We don't want that while we are first populating the object
                        drawableNode.setIsInitialising(true);

                        PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("select node_id,object_name,object_class,object_value from node_details where node_id = ?");
                        preparedStatement.setInt(1, nodeResultSet.getInt("id"));
                        ResultSet nodeDetailsResultSet = preparedStatement.executeQuery();
                        while (nodeDetailsResultSet.next()) {
                            Method method;
                            try {
                                if ("java.lang.Double".equals(nodeDetailsResultSet.getString("object_class"))) {
                                    Double doubleValue = nodeDetailsResultSet.getDouble("object_value");

                                    method = drawableNode.getClass().getMethod("set" + nodeDetailsResultSet.getString("object_name"), Class.forName(nodeDetailsResultSet.getString("object_class")));
                                    method.invoke(drawableNode, doubleValue);
                                } else if ("java.lang.String".equals(nodeDetailsResultSet.getString("object_class"))) {
                                    String stringValue = nodeDetailsResultSet.getString("object_value");

                                    method = drawableNode.getClass().getMethod("set" + nodeDetailsResultSet.getString("object_name"), Class.forName(nodeDetailsResultSet.getString("object_class")));
                                    method.invoke(drawableNode, stringValue);
                                } else if ("java.lang.Integer".equals(nodeDetailsResultSet.getString("object_class"))) {
                                    Integer integerValue = nodeDetailsResultSet.getInt("object_value");

                                    method = drawableNode.getClass().getMethod("set" + nodeDetailsResultSet.getString("object_name"), Class.forName(nodeDetailsResultSet.getString("object_class")));
                                    method.invoke(drawableNode, integerValue);
                                }
                            } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }

                        drawableNode.setIsInitialising(false);
                    }
                }

                flowController.setStartNode(flowController.getNodeById(startNode));
                addProgram(loadedProgram);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadSwitches(SwitchNode switchNode) {
        List<Switch> aSwitches = new ArrayList<>();

        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("select id, target, enabled from switch where node_id = ?");
            preparedStatement.setInt(1, switchNode.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                aSwitches.add(new Switch(resultSet.getInt("id"), switchNode, resultSet.getString("target"), resultSet.getBoolean("enabled")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        switchNode.setSwitches(aSwitches);
    }

    public static void saveSwitch(Switch aSwitch) {
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("update switch set target = ?, enabled = ? where id = ?");
            if (preparedStatement != null) {
                preparedStatement.setString(1, aSwitch.getTarget());
                preparedStatement.setBoolean(2, aSwitch.isEnabled());
                preparedStatement.setInt(3, aSwitch.getId());
                int result = preparedStatement.executeUpdate();
                preparedStatement.close();

                if (result == 0) { // If record does not exist insert a new one..
                    aSwitch.setId(getNextId("switch")); // Gets the next ID for a node that is about to be created

                    preparedStatement = mySQLInstance.getPreparedStatement("insert into switch values (default, ?, ?, ?)");
                    if (preparedStatement != null) {
                        preparedStatement.setInt(1, aSwitch.getParent().getId());
                        preparedStatement.setString(2, aSwitch.getTarget());
                        preparedStatement.setBoolean(3, aSwitch.isEnabled());
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Switch createNewSwitch(String target, SwitchNode parent, Boolean enabled) {
        Switch aSwitch = new Switch(getNextId("switch"), parent, target, enabled);

        parent.addSwitch(aSwitch);
        DataBank.saveSwitch(aSwitch);

        return aSwitch;
    }

    public static void loadInputs(InputNode inputNode) {
        List<Input> inputs = new ArrayList<>();

        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("select id, variable_name, variable_value from input where node_id = ?");
            preparedStatement.setInt(1, inputNode.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                inputs.add(new Input(resultSet.getInt("id"), resultSet.getString("variable_name"), resultSet.getString("variable_value"), inputNode));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        inputNode.setInputs(inputs);
    }

    public static void saveInput(Input input) {
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("update input set variable_name = ?, variable_value = ? where id = ?");
            if (preparedStatement != null) {
                preparedStatement.setString(1, input.getVariableName());
                preparedStatement.setString(2, input.getVariableValue());
                preparedStatement.setInt(3, input.getId());
                int result = preparedStatement.executeUpdate();
                preparedStatement.close();

                if (result == 0) { // If record does not exist insert a new one..
                    input.setId(getNextId("input")); // Gets the next ID for a node that is about to be created

                    preparedStatement = mySQLInstance.getPreparedStatement("insert into input values (default, ?, ?, ?)");
                    if (preparedStatement != null) {
                        preparedStatement.setInt(1, input.getParent().getId());
                        preparedStatement.setString(2, input.getVariableName());
                        preparedStatement.setString(3, input.getVariableValue());
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Input createNewInput(String variableName, String variableValue, InputNode parent) {
        Input input = new Input(getNextId("trigger_condition"), variableName, variableValue, parent);

        parent.addInput(input);
        DataBank.saveInput(input);

        return input;
    }

    public static void loadTriggers(TriggerNode triggerNode) {
        List<Trigger> triggers = new ArrayList<>();

        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("select id, trigger_watch, trigger_when, trigger_then from trigger_condition where node_id = ?");
            preparedStatement.setInt(1, triggerNode.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                triggers.add(new Trigger(resultSet.getInt("id"), resultSet.getString("trigger_watch"), resultSet.getString("trigger_when"), resultSet.getString("trigger_then"), triggerNode));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        triggerNode.setTriggers(triggers);
    }

    public static void saveTrigger(Trigger trigger) {
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("update trigger_condition set trigger_watch = ?, trigger_when = ?, trigger_then = ? where id = ?");
            if (preparedStatement != null) {
                preparedStatement.setString(1, trigger.getWatch());
                preparedStatement.setString(2, trigger.getWhen());
                preparedStatement.setString(3, trigger.getThen());
                preparedStatement.setInt(4, trigger.getId());
                int result = preparedStatement.executeUpdate();
                preparedStatement.close();

                if (result == 0) { // If record does not exist insert a new one..
                    trigger.setId(getNextId("trigger_condition")); // Gets the next ID for a node that is about to be created

                    preparedStatement = mySQLInstance.getPreparedStatement("insert into trigger_condition values (default, ?, ?, ?, ?)");
                    if (preparedStatement != null) {
                        preparedStatement.setInt(1, trigger.getParent().getId());
                        preparedStatement.setString(2, trigger.getWatch());
                        preparedStatement.setString(3, trigger.getWhen());
                        preparedStatement.setString(4, trigger.getThen());
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Trigger createNewTrigger(String watch, String when, String then, TriggerNode parent) {
        Trigger trigger = new Trigger(getNextId("trigger_condition"), watch, when, then, parent);

        parent.addTrigger(trigger);
        DataBank.saveTrigger(trigger);

        return trigger;
    }

    public static void loadNodeColours(NodeColours nodeColours) {
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("select id, colour_r, colour_g, colour_b, node_type from node_colour");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                NodeColour nodeColour = new NodeColour(resultSet.getInt("colour_r"), resultSet.getInt("colour_g"), resultSet.getInt("colour_b"), resultSet.getString("node_type"));
                nodeColours.addNodeColour(nodeColour);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveNodeColour(NodeColour nodeColour) {
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("update node_colour set colour_r = ?, colour_g = ?, colour_b = ? where node_type = ?");
            if (preparedStatement != null) {
                preparedStatement.setInt(1, nodeColour.getRed());
                preparedStatement.setInt(2, nodeColour.getGreen());
                preparedStatement.setInt(3, nodeColour.getBlue());
                preparedStatement.setString(4, nodeColour.getNodeType());
                int result = preparedStatement.executeUpdate();
                preparedStatement.close();

                if (result == 0) { // If record does not exist insert a new one..
                    nodeColour.setId(getNextId("node_colour")); // Gets the next ID for a node that is about to be created

                    preparedStatement = mySQLInstance.getPreparedStatement("insert into node_colour values (default, ?, ?, ?, ?)");
                    if (preparedStatement != null) {
                        preparedStatement.setString(1, nodeColour.getNodeType());
                        preparedStatement.setInt(2, nodeColour.getRed());
                        preparedStatement.setInt(3, nodeColour.getGreen());
                        preparedStatement.setInt(4, nodeColour.getBlue());
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Integer getNextId(String tableName) {
        Integer autoIncrement = -1;
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            ResultSet resultSet = mySQLInstance.runQuery("SELECT AUTO_INCREMENT FROM information_schema.tables WHERE table_name = '" + tableName + "' AND table_schema = DATABASE();");
            while (resultSet.next()) {
                autoIncrement = resultSet.getInt("AUTO_INCREMENT");
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return autoIncrement;
    }

    public static NodeColours getNodeColours() {
        return nodeColours;
    }
}