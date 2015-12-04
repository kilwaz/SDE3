package application.utils.managers;

import application.data.model.DatabaseObject;
import org.apache.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseObjectManager {
    private static DatabaseObjectManager instance;
    public ConcurrentHashMap<String, DatabaseObject> databaseObjects;

    private static Logger log = Logger.getLogger(DatabaseObjectManager.class);

    public DatabaseObjectManager() {
        instance = this;
        databaseObjects = new ConcurrentHashMap<>();
    }

    public Boolean objectExists(UUID uuid) {
        return databaseObjects.containsKey(uuid.toString());
    }

    public DatabaseObject getDatabaseObject(UUID uuid) {
//        log.info("Got object " + uuid + " -> " + databaseObjects.get(uuid.toString()));
        return databaseObjects.get(uuid.toString());
    }

    public void addObject(DatabaseObject databaseObject) {
//        log.info("Added object " + databaseObject.getUuid() + " -> " + databaseObject);
        databaseObjects.put(databaseObject.getUuidString(), databaseObject);
    }

    public void removeObject(DatabaseObject databaseObject) {
        databaseObjects.remove(databaseObject.getUuid().toString());
    }

    public static DatabaseObjectManager getInstance() {
        return instance;
    }

    public void clearAllObjects() {
        databaseObjects.clear();
    }
}
