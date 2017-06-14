package sde.application.data.model;

import org.apache.log4j.Logger;
import sde.application.error.Error;
import sde.application.utils.managers.DatabaseObjectManager;

import java.util.UUID;

public class DatabaseObject {
    private static Logger log = Logger.getLogger(DatabaseObject.class);
    private UUID uuid = UUID.randomUUID();

    public DatabaseObject() {
        // Here the UUID is generated for us
    }

    public DatabaseObject(UUID uuid) {
        this.uuid = uuid;
        this.load();
    }

    public static <DBObject extends DatabaseObject> DBObject load(UUID uuid, Class<DBObject> clazz) {
        DatabaseObjectManager databaseObjectManager = DatabaseObjectManager.getInstance();
        DatabaseObject loadedObject;

//        try {
//            databaseObject = databaseObjectManager.getDatabaseObjects().get(uuid.toString(), () -> {
//                DatabaseObject loadedObject = create(clazz);
//                loadedObject.setUuid(uuid);
//                //loadedObject.load();
//
//                return loadedObject;
//            });
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        if (databaseObject != null) {
//            databaseObject.load();
//        }

        if (databaseObjectManager.objectExists(uuid)) {
            loadedObject = databaseObjectManager.getDatabaseObject(uuid);
        } else {
            loadedObject = create(clazz);
            loadedObject.setUuid(uuid);
            databaseObjectManager.addObject(loadedObject);
            loadedObject.load();
        }

        return (DBObject) loadedObject;
    }

    public static <DBObject extends DatabaseObject> DBObject create(Class<DBObject> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Error.CREATE_NEW_INSTANCE_ERROR.record().create(ex);
        }

        return null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setUuidFromString(String uuidStr) {
        uuid = UUID.fromString(uuidStr);
    }

    public String getUuidStringWithoutHyphen() {
        return uuid.toString().replace("-", "");
    }

    public String getUuidString() {
        return uuid.toString();
    }

    public void scheduleToSave() {

    }

    public void save() {
        new DatabaseAction<>().save(this, DatabaseLink.getNewInstanceFromBaseClass(this.getClass()));
    }

    public void delete() {
        new DatabaseAction<>().delete(this, DatabaseLink.getNewInstanceFromBaseClass(this.getClass()));
    }

    public void deleteCascade() {
        new DatabaseAction<>().deleteCascade(this, DatabaseLink.getNewInstanceFromBaseClass(this.getClass()));
    }

    public void load() {
        new DatabaseAction<>().load(this, DatabaseLink.getNewInstanceFromBaseClass(this.getClass()));
    }
}
