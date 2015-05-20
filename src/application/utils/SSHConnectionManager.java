package application.utils;

import application.net.ssh.SSHManager;

import java.util.ArrayList;
import java.util.List;

public class SSHConnectionManager {
    private static SSHConnectionManager SSHConnectionManager;
    private List<SSHManager> openConnections;

    public SSHConnectionManager() {
        SSHConnectionManager = this;
        openConnections = new ArrayList<>();
    }

    public void addConnection(SSHManager sshManager) {
        openConnections.add(sshManager);
    }

    public void closeConnections() {
        openConnections.forEach(SSHManager::close);
    }

    public static SSHConnectionManager getInstance() {
        return SSHConnectionManager;
    }
}
