package sde.application.utils.managers;

import org.apache.log4j.Logger;
import sde.application.test.selenium.NodeHelperClient;

import java.util.HashMap;

public class SeleniumNodeHelperManager {
    private static SeleniumNodeHelperManager instance = null;
    private static Logger log = Logger.getLogger(SeleniumNodeHelperManager.class);
    private HashMap<String, NodeHelperClient> clientHelpers = new HashMap<>();

    public SeleniumNodeHelperManager() {
        instance = this;
    }

    public NodeHelperClient connectToNodeHelper(String host) {
        NodeHelperClient nodeHelperClient = new NodeHelperClient(host);
        nodeHelperClient.execute();
        clientHelpers.put(host, nodeHelperClient);
        return nodeHelperClient;
    }

    public NodeHelperClient getNodeHelper(String host) {
        if (!clientHelpers.containsKey(host)) {
            connectToNodeHelper(host);
        }

        return clientHelpers.get(host);
    }

    public static SeleniumNodeHelperManager getInstance() {
        if (instance == null) {
            instance = new SeleniumNodeHelperManager();
        }

        return instance;
    }
}
