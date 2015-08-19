package application.utils.managers;

import application.data.NetworkNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class NetworkManager {
    private static NetworkManager NetworkManager;
    private List<NetworkNodeInfo> networkList;

    public NetworkManager() {
        NetworkManager = this;
        networkList = new ArrayList<>();
    }

    public void addNetworkNodeInfo(NetworkNodeInfo networkNodeInfo) {
        networkList.add(networkNodeInfo);
    }

    public static NetworkManager getInstance() {
        return NetworkManager;
    }
}
