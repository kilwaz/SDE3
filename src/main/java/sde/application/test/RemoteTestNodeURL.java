package sde.application.test;

import org.apache.log4j.Logger;

public class RemoteTestNodeURL {
    private static Logger log = Logger.getLogger(RemoteTestNodeURL.class);

    private Integer hubPort = 4444; // Default hubPort for selenium grid
    private String hubIp = "localhost";

    public RemoteTestNodeURL() {

    }

    public void setHost(String host) {
        if (host == null) {
            return;
        }
        if (host.contains(":")) { // If the hubPort is specified then we use it
            String[] split = host.split(":");
            hubIp = split[0];
            hubPort = Integer.parseInt(split[1]);
        } else {
            hubIp = host;
        }
    }

    public RemoteTestNodeURL(String host) {
        setHost(host);
    }

    public Integer getHubPort() {
        return hubPort;
    }

    public String getHubIp() {
        return hubIp;
    }

    public String getRegistrationURL() {
        return "http://" + hubIp + ":" + hubPort + getPath();
    }

    public String getAddress() {
        return hubIp + ":" + hubPort;
    }

    public String getHttpAddress() {
        return "http://" + hubIp + ":" + hubPort;
    }

    public String getPath() {
        return "/wd/hub";
    }
}
