package sde.application.data;

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 20/10/14
 * Time: 16:25
 * To change this template use File | Settings | File Templates.
 */
public class NetworkNodeInfo {

    private String hostAddress;
    private String hostName;
    private Boolean reachable;


    public NetworkNodeInfo() {

    }

    public NetworkNodeInfo(String hostAddress, String hostName, Boolean reachable) {
        this.hostAddress = hostAddress;
        this.hostName = hostName;
        this.reachable = reachable;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Boolean isReachable() {
        return reachable;
    }

    public void setReachable(Boolean reachable) {
        this.reachable = reachable;
    }
}
