package sde.application.data.model;

import sde.application.data.*;
import sde.application.data.model.dao.DAO;
import sde.application.data.model.dao.DrawableNodeDAO;
import sde.application.error.Error;
import sde.application.gui.Program;
import sde.application.node.design.DrawableNode;
import sde.application.node.objects.LinkedTestCase;
import sde.application.node.objects.datatable.DataTableRow;
import sde.application.utils.managers.DatabaseObjectManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseAction<DBObject extends DatabaseObject, DBLink extends DatabaseLink> {
    private static Logger log = Logger.getLogger(DatabaseAction.class);

    private static ConcurrentHashMap<String, DelayedLoad> delayedLoadedObjects = new ConcurrentHashMap<>();

    public void save(DBObject dbObject, DBLink dbLink) {
        // Building the update query string
        StringBuilder updateQueryBuilder = new StringBuilder();
        updateQueryBuilder.append("update ")
                .append(dbLink.getTableName());
        Boolean firstColumn = true;
        for (ModelColumn modelColumn : dbLink.getModelColumns()) {
            if (firstColumn) {
                updateQueryBuilder.append(" set ")
                        .append(modelColumn.getColumnName())
                        .append(" = ?");
                firstColumn = false;
            } else {
                updateQueryBuilder.append(", ")
                        .append(modelColumn.getColumnName())
                        .append(" = ?");
            }
        }

        updateQueryBuilder.append(" where uuid = ?");

        // Building the update query object
        UpdateQuery updateQuery = new UpdateQuery(updateQueryBuilder.toString());

        for (ModelColumn modelColumn : dbLink.getModelColumns()) {
            try {
                updateQuery.addParameter(modelColumn.getObjectSaveMethod().invoke(dbObject));
            } catch (IllegalAccessException | InvocationTargetException ex) {
                Error.DATABASE_OBJECT_METHOD_NOT_FOUND
                        .record()
                        .additionalInformation("Column: " + modelColumn.getColumnName())
                        .additionalInformation("Method: " + modelColumn.getObjectSaveMethod().getName())
                        .additionalInformation("Linked Class: " + dbLink.getLinkClass())
                        .create(ex);
            }
        }

        // The id to update
        updateQuery.addParameter(dbObject.getUuidString());

        // Execute the update query
        UpdateResult updateResult = (UpdateResult) updateQuery.execute();

        // If record does not exist insert a new one..
        if (updateResult.getResultNumber() == 0) {
            // Build the insert statement
            updateQueryBuilder = new StringBuilder();
            updateQueryBuilder.append("insert into ")
                    .append(dbLink.getTableName())
                    .append("(");

            firstColumn = true;
            for (ModelColumn modelColumn : dbLink.getModelColumns()) {
                if (firstColumn) {
                    updateQueryBuilder.append(modelColumn.getColumnName());
                    firstColumn = false;
                } else {
                    updateQueryBuilder.append(",").append(modelColumn.getColumnName());
                }
            }

            updateQueryBuilder.append(")")
                    .append(" values (?")
                    .append(StringUtils.repeat(", ?", dbLink.getModelColumns().size() - 1))
                    .append(")");

            // Create query object and fill in parameters
            UpdateQuery insertQuery = new UpdateQuery(updateQueryBuilder.toString());
            for (ModelColumn modelColumn : dbLink.getModelColumns()) {
                try {
                    insertQuery.addParameter(modelColumn.getObjectSaveMethod().invoke(dbObject));
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    Error.DATABASE_OBJECT_METHOD_NOT_FOUND.record().create(ex);
                }
            }

            // Run the insert query
            insertQuery.execute();
        }
    }

    public void delete(DBObject dbObject, DBLink dbLink) {
        // Create query object and fill in parameters
        UpdateQuery deleteQuery = new UpdateQuery("delete from " + dbLink.getTableName() + " where uuid = ?");
        for (ModelColumn modelColumn : dbLink.getModelColumns()) {
            try {
                if ("uuid".equals(modelColumn.getColumnName())) {
                    deleteQuery.addParameter(modelColumn.getObjectSaveMethod().invoke(dbObject));
                }
            } catch (IllegalAccessException | InvocationTargetException ex) {
                Error.DATABASE_OBJECT_METHOD_NOT_FOUND.record().create(ex);
            }
        }

        // Run the delete query
        deleteQuery.execute();
    }

    public void load(DBObject dbObject, DBLink dbLink) {
        // Building the select query string
        StringBuilder selectQueryBuilder = new StringBuilder();
        selectQueryBuilder.append("select ");
        Boolean firstColumn = true;
        for (ModelColumn modelColumn : dbLink.getModelColumns()) {
            if (firstColumn) {
                selectQueryBuilder
                        .append(modelColumn.getColumnName())
                        .append(" ");
                firstColumn = false;
            } else {
                selectQueryBuilder.append(", ")
                        .append(modelColumn.getColumnName())
                        .append(" ");
            }
        }
        selectQueryBuilder
                .append("from ")
                .append(dbLink.getTableName())
                .append(" where uuid = ?");

        SelectResult selectResult = (SelectResult) new SelectQuery(selectQueryBuilder.toString())
                .addParameter(dbObject.getUuidString())
                .execute();


        for (SelectResultRow resultRow : selectResult.getResults()) {
            for (ModelColumn modelColumn : dbLink.getModelColumns()) {
                try {
                    if (modelColumn.getObjectLoadMethod() != null) {
                        Class[] loadMethodParameter = modelColumn.getObjectLoadMethod().getParameterTypes();
                        Class loadParameterClass = null;

                        if (loadMethodParameter.length > 0) {
                            loadParameterClass = loadMethodParameter[0];
                        }
                        if (loadParameterClass != null) {
                            if (loadParameterClass.equals(String.class)) {  // STRING
                                modelColumn.getObjectLoadMethod().invoke(dbObject, resultRow.getString(modelColumn.getColumnName()));
                            } else if (loadParameterClass.equals(Double.class)) { // DOUBLE
                                modelColumn.getObjectLoadMethod().invoke(dbObject, resultRow.getDouble(modelColumn.getColumnName()));
                            } else if (loadParameterClass.equals(Integer.class)) { // INTEGER
                                modelColumn.getObjectLoadMethod().invoke(dbObject, resultRow.getInt(modelColumn.getColumnName()));
                            } else if (loadParameterClass.equals(Boolean.class)) { // BOOLEAN
                                modelColumn.getObjectLoadMethod().invoke(dbObject, resultRow.getBoolean(modelColumn.getColumnName()));
                            } else if (loadParameterClass.equals(UUID.class)) { // UUID
                                String uuid = resultRow.getString(modelColumn.getColumnName());
                                if (uuid != null && !uuid.isEmpty()) {
                                    modelColumn.getObjectLoadMethod().invoke(dbObject, DAO.UUIDFromString(uuid));
                                }
                            } else if (loadParameterClass.equals(User.class)) { // USER
                                String uuidStr = resultRow.getString(modelColumn.getColumnName());
                                if (uuidStr != null && !uuidStr.isEmpty()) {
                                    User user = loadCachedObject(uuidStr, User.class);
                                    modelColumn.getObjectLoadMethod().invoke(dbObject, user);
                                }
                            } else if (loadParameterClass.equals(DataTableRow.class)) { // DATA TABLE ROW
                                String uuidStr = resultRow.getString(modelColumn.getColumnName());
                                if (uuidStr != null && !uuidStr.isEmpty()) {
                                    DataTableRow user = loadCachedObject(uuidStr, DataTableRow.class);
                                    modelColumn.getObjectLoadMethod().invoke(dbObject, user);
                                }
                            } else if (loadParameterClass.equals(Program.class)) { // PROGRAM
                                String uuidStr = resultRow.getString(modelColumn.getColumnName());
                                if (uuidStr != null && !uuidStr.isEmpty()) {
                                    Program program = loadCachedObject(uuidStr, Program.class);
                                    modelColumn.getObjectLoadMethod().invoke(dbObject, program);
                                }
                            } else if (loadParameterClass.equals(LinkedTestCase.class)) { // LINKEDTESTCASE
                                String uuidStr = resultRow.getString(modelColumn.getColumnName());
                                if (uuidStr != null && !uuidStr.isEmpty()) {
                                    LinkedTestCase linkedTestCase = loadCachedObject(uuidStr, LinkedTestCase.class);
                                    modelColumn.getObjectLoadMethod().invoke(dbObject, linkedTestCase);
                                }
                            } else if (loadParameterClass.equals(DrawableNode.class) || loadParameterClass.getSuperclass() != null && loadParameterClass.getSuperclass().equals(DrawableNode.class)) { // DRAWABLE NODE
                                String uuidStr = resultRow.getString(modelColumn.getColumnName());
                                if (uuidStr != null && !uuidStr.isEmpty()) {
                                    DrawableNodeDAO drawableNodeDAO = new DrawableNodeDAO();
                                    DrawableNode drawableNode = drawableNodeDAO.getDrawableNodeUnknownClassFromUuid(DAO.UUIDFromString(uuidStr));
                                    modelColumn.getObjectLoadMethod().invoke(dbObject, drawableNode);
                                }
                            } else if (loadParameterClass.equals(Object.class)) { // OBJECT
                                modelColumn.getObjectLoadMethod().invoke(dbObject, resultRow.getColumnObject(modelColumn.getColumnName()));
                            }
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    Error.DATABASE_OBJECT_METHOD_NOT_FOUND.record().create(ex);
                } catch (IllegalArgumentException ex) {
                    Error.DATABASE_OBJECT_METHOD_NOT_FOUND.record().additionalInformation("Method name: " + modelColumn.getObjectLoadMethod().getName()).create(ex);
                }
            }
        }
    }

    private <DBObject extends DatabaseObject> DBObject loadCachedObject(String uuidStr, Class<DBObject> clazz) {
        DatabaseObjectManager databaseObjectManager = DatabaseObjectManager.getInstance();
        if (uuidStr != null && !uuidStr.isEmpty()) {
            UUID uuid = DAO.UUIDFromString(uuidStr);
            DatabaseObject databaseObject = null;

//            try {
//                databaseObject = databaseObjectManager.getDatabaseObjects().get(uuid.toString(), () -> DatabaseObject.load(uuid, clazz));
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }

            if (databaseObjectManager.objectExists(uuid)) {
                databaseObject = databaseObjectManager.getDatabaseObject(uuid);
            } else {
                if (!delayedLoadedObjects.containsKey(uuid.toString())) {
                    delayedLoadedObjects.put(uuid.toString(), new DelayedLoad(uuid.toString(), clazz, null));
                    databaseObject = DatabaseObject.load(uuid, clazz);
                    databaseObjectManager.addObject(databaseObject);
                    delayedLoadedObjects.remove(uuid.toString());
                }
            }

            return (DBObject) databaseObject;
        }
        return null;
    }
}