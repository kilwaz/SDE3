package sde.application.utils.managers;

import sde.application.net.ssh.SSHManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        List<SSHManager> sshManagerToClose = openConnections.stream().filter(sshManager -> !sshManager.isConnected()).collect(Collectors.toList());

        openConnections.removeAll(sshManagerToClose);
    }

    public static SSHConnectionManager getInstance() {
        return SSHConnectionManager;
    }
}
