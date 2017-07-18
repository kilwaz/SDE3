package sde.application.net.objects;

import sde.application.error.Error;
import sde.application.net.objects.messages.client.Command;
import sde.application.net.objects.messages.client.RetrieveRecording;
import sde.application.net.objects.messages.initilise.ClientHello;
import sde.application.net.objects.messages.initilise.ServerHello;
import sde.application.net.objects.messages.server.Recording;

import java.util.HashMap;

public class ObjectTypes {
    private static HashMap<Class, Integer> objectTypes = new HashMap<>();

    // Defined objects
    static {
        objectTypes.put(Command.class, 1);
        objectTypes.put(ServerHello.class, 2);
        objectTypes.put(ClientHello.class, 3);
        objectTypes.put(RetrieveRecording.class, 4);
        objectTypes.put(Recording.class, 5);
    }

    public static Integer resolveObjectType(NetworkObject networkObject) {
        if (objectTypes.containsKey(networkObject.getClass())) {
            networkObject.setObjectType(objectTypes.get(networkObject.getClass()));
        } else {
            Error.UNABLE_TO_RESOLVE_NETWORK_OBJECT_CLASS.record().create();
        }
        return -1;
    }
}
