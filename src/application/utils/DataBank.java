package application.utils;

import application.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBank {
    static public Program currentlyEditProgram;
    static private HashMap<Integer, Program> programs = new HashMap<Integer, Program>();
    static private HashMap<String, HashMap<String, Object>> programVariables = new HashMap<String, HashMap<String, Object>>();
    static private HashMap<String, HashMap<String, Object>> programInstances = new HashMap<String, HashMap<String, Object>>();
    static private HashMap<String, HashMap<String, Object>> testResultInstances = new HashMap<String, HashMap<String, Object>>();
    static private MySQLConnectionManager mySQLInstance;

    static public List<String> getProgramNames() {
        List<String> nameList = new ArrayList<String>();

        for (Program program : programs.values()) {
            nameList.add(program.getName());
        }

        return nameList;
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

    static public List<Program> getPrograms() {
        return new ArrayList(programs.values());
    }

    public static void saveVariable(String name, Object object, String referenceID) {
        HashMap<String, Object> programVariable = programVariables.get(referenceID);
        if (programVariable == null) {
            programVariable = new HashMap<String, Object>();
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
            instances = new HashMap<String, Object>();
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

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("update program set name = ?, start_node = ? where id = ?");
            if (preparedStatement != null) {
                preparedStatement.setString(1, program.getName());
                preparedStatement.setInt(2, program.getFlowController().getStartNode().getId());
                preparedStatement.setInt(3, program.getId());
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

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("insert into program values (default, ?, NULL)");
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
                            preparedStatement.setString(3, savableAttribute.getClassName());
                            preparedStatement.setObject(4, savableAttribute.getVariable());
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

            ResultSet resultSet = mySQLInstance.runQuery("select id,name,start_node from program;");
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                Integer programId = resultSet.getInt("id");
                Integer startNode = resultSet.getInt("start_node");
                Program loadedProgram = new Program(name, programId);
                ResultSet sourceResultSet = mySQLInstance.runQuery("select id,program_id,node_type from node where program_id = '" + programId + "';");
                FlowController flowController = loadedProgram.getFlowController();

                while (sourceResultSet.next()) {
                    DrawableNode drawableNode = flowController.createNewNode(
                            sourceResultSet.getInt("id"),
                            sourceResultSet.getInt("program_id"),
                            sourceResultSet.getString("node_type"),
                            true
                    );

                    if (drawableNode != null) {
                        PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("select node_id,object_name,object_class,object_value from node_details where node_id = ?");
                        preparedStatement.setInt(1, sourceResultSet.getInt("id"));
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
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("Error loading node id " + sourceResultSet.getInt("id") + " program id " + sourceResultSet.getInt("program_id") + " node type " + sourceResultSet.getString("node_type") + " - Node type is not recognised");
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

    public static void loadSplits(SwitchNode switchNode) {
        List<Switch> aSwitches = new ArrayList<Switch>();

        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("select id, target, enabled from split where node_id = ?");
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

    public static void saveSplit(Switch aSwitch) {
        try {
            if (mySQLInstance == null) {
                mySQLInstance = MySQLConnectionManager.getInstance();
            }

            PreparedStatement preparedStatement = mySQLInstance.getPreparedStatement("update split set target = ?, enabled = ? where id = ?");
            if (preparedStatement != null) {
                preparedStatement.setString(1, aSwitch.getTarget());
                preparedStatement.setBoolean(2, aSwitch.isEnabled());
                preparedStatement.setInt(3, aSwitch.getId());
                int result = preparedStatement.executeUpdate();
                preparedStatement.close();

                if (result == 0) { // If record does not exist insert a new one..
                    aSwitch.setId(getNextId("split")); // Gets the next ID for a node that is about to be created

                    preparedStatement = mySQLInstance.getPreparedStatement("insert into split values (default, ?, ?, ?)");
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

    public static Switch createNewSplit(String target, SwitchNode parent, Boolean enabled) {
        Switch aSwitch = new Switch(getNextId("split"), parent, target, enabled);

        parent.addSplit(aSwitch);
        DataBank.saveSplit(aSwitch);

        return aSwitch;
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
}