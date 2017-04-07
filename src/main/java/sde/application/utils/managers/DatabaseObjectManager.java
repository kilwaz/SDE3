package sde.application.utils.managers;

import sde.application.data.model.DatabaseObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.log4j.Logger;

import java.util.UUID;

public class DatabaseObjectManager {
    private static DatabaseObjectManager instance;
    private static Logger log = Logger.getLogger(DatabaseObjectManager.class);
    private Cache<String, DatabaseObject> databaseObjects;

    public DatabaseObjectManager() {
        instance = this;
        int numProcessors = Runtime.getRuntime().availableProcessors();
        databaseObjects = CacheBuilder
                .newBuilder()
                .concurrencyLevel(numProcessors * 2)
                .weakValues()
                .build();
    }

    public static DatabaseObjectManager getInstance() {
        return instance;
    }

    public Boolean objectExists(UUID uuid) {
        return getDatabaseObject(uuid) != null;
    }

    public DatabaseObject getDatabaseObject(UUID uuid) {
        return databaseObjects.getIfPresent(uuid.toString());
    }

    public void addObject(DatabaseObject databaseObject) {
        databaseObjects.put(databaseObject.getUuidString(), databaseObject);
    }

    public void removeObject(DatabaseObject databaseObject) {
        databaseObjects.invalidate(databaseObject.getUuid().toString());
    }

    public void clearAllObjects() {
        databaseObjects.invalidateAll();
    }
}
