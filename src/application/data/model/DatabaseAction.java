package application.data.model;

import application.data.DataBank;
import application.data.UpdateQuery;
import application.data.UpdateResult;
import application.error.Error;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;

public class DatabaseAction<DBObject extends DatabaseObject, DBLink extends DatabaseLink> {
    private static Logger log = Logger.getLogger(DatabaseAction.class);

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

        updateQueryBuilder.append(" where id = ?");

        // Building the update query object
        UpdateQuery updateQuery = new UpdateQuery(updateQueryBuilder.toString());

        for (ModelColumn modelColumn : dbLink.getModelColumns()) {
            try {
                updateQuery.addParameter(modelColumn.getObjectMethod().invoke(dbObject));
            } catch (IllegalAccessException | InvocationTargetException ex) {
                Error.DATABASE_OBJECT_METHOD_NOT_FOUND.record().create(ex);
            }
        }

        // The id to update
        updateQuery.addParameter(dbObject.getId());

        // Execute the update query
        UpdateResult updateResult = (UpdateResult) updateQuery.execute();

        // If record does not exist insert a new one..
        if (updateResult.getResultNumber() == 0) {
            dbObject.setId(DataBank.getNextId(dbLink.getTableName()));

            // Build the insert statement
            updateQueryBuilder = new StringBuilder();
            updateQueryBuilder.append("insert into ")
                    .append(dbLink.getTableName())
                    .append(" values (default")
                    .append(StringUtils.repeat(", ?", dbLink.getModelColumns().size()))
                    .append(")");

            // Create query object and fill in parameters
            UpdateQuery insertQuery = new UpdateQuery(updateQueryBuilder.toString());
            for (ModelColumn modelColumn : dbLink.getModelColumns()) {
                try {
                    insertQuery.addParameter(modelColumn.getObjectMethod().invoke(dbObject));
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    Error.DATABASE_OBJECT_METHOD_NOT_FOUND.record().create(ex);
                }
            }

            // Run the insert query
            insertQuery.execute();
        }
    }
}
