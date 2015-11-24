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
        if (user == null) { // Initial user does not exist, we need to create it
            user = User.create(User.class);
            user.setUsername(AppParams.CURRENT_USER);
            user.save();
        }
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

    public static NodeColours getNodeColours() {
        return nodeColours;
    }
}