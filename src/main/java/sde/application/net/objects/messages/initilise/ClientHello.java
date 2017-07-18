package sde.application.net.objects.messages.initilise;

import org.apache.log4j.Logger;
import sde.application.net.objects.NetworkObject;

import java.util.ArrayList;
import java.util.List;

public class ClientHello extends NetworkObject {
    private static Logger log = Logger.getLogger(ClientHello.class);

    public ClientHello() {
        super();
    }

    public List<NetworkObject> getResponses() {
        List<NetworkObject> networkObjects = new ArrayList<>();
        networkObjects.add(new ServerHello());
        return networkObjects;
    }
}
