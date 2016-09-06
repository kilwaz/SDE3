package application.utils.managers;

import application.data.NetworkNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class NetworkManager {
    private static NetworkManager instance;
    private List<NetworkNodeInfo> networkList;

    public NetworkManager() {
        instance = this;
        networkList = new ArrayList<>();
    }

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void addNetworkNodeInfo(NetworkNodeInfo networkNodeInfo) {
        networkList.add(networkNodeInfo);
    }
}
