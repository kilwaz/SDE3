package application.utils;

import application.data.NetworkNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 20/10/14
 * Time: 16:17
 * To change this template use File | Settings | File Templates.
 */
public class NetworkManager {
    private static NetworkManager NetworkManager;
    private List<NetworkNodeInfo> networkList;

    public NetworkManager() {
        NetworkManager = this;
        networkList = new ArrayList<NetworkNodeInfo>();
    }

    public void addNetworkNodeInfo(NetworkNodeInfo networkNodeInfo) {
        networkList.add(networkNodeInfo);
    }

    public static NetworkManager getInstance() {
        return NetworkManager;
    }
}
