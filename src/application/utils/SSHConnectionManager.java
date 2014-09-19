package application.utils;

import application.net.SSHManager;

import java.util.ArrayList;
import java.util.List;

public class SSHConnectionManager {
    private static SSHConnectionManager SSHConnectionManager;
    private List<SSHManager> openConnections;

    public SSHConnectionManager() {
        SSHConnectionManager = this;
        openConnections = new ArrayList<SSHManager>();
    }

    public void addConnection(SSHManager sshManager) {
        openConnections.add(sshManager);
    }

    public void closeConnections() {
        System.out.println("Closing Connections!");
        for (SSHManager connection : openConnections) {
            connection.close();
        }
    }

    public static SSHConnectionManager getInstance() {
        return SSHConnectionManager;
    }
}
