package sde.application.data;

import sde.application.data.model.dao.UserDAO;
import sde.application.error.Error;
import sde.application.node.implementations.CustomObjectNode;
import sde.application.node.implementations.FileStoreNode;
import sde.application.node.objects.SDEFile;
import sde.application.utils.AppParams;
import sde.application.utils.managers.SessionManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.io.File;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class DataBank {
    private static Logger log = Logger.getLogger(DataBank.class);

    private static HashMap<String, HashMap<String, Object>> programVariables = new HashMap<>();
    private static HashMap<String, HashMap<String, Object>> programInstances = new HashMap<>();
    private static NodeColours nodeColours = new NodeColours();

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
        session.setSelectedProgram(user.getCurrentProgram());
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
                    DBConnectionManager.getInstance().getApplicationConnection().getConnection().setAutoCommit(false);
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
                } else if (value instanceof DateTime) {
                    java.sql.Date date = new java.sql.Date(((DateTime) value).getMillis());
                    preparedStatement.setDate(valueCount, date);
                } else if (value == null) {
                    preparedStatement.setObject(valueCount, null);
                }

                valueCount++;
            }
        }
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

    public static NodeColours getNodeColours() {
        return nodeColours;
    }
}