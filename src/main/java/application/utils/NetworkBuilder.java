package application.utils;

import application.data.NetworkNodeInfo;
import application.error.Error;
import application.utils.managers.NetworkManager;
import org.apache.log4j.Logger;

import java.net.InetAddress;

public class NetworkBuilder {
    private String networkAddress = "0.0.0.";

    private static Logger log = Logger.getLogger(NetworkBuilder.class);

    public NetworkBuilder() {
        try {
            if (InetAddress.getLocalHost().getHostAddress() != null) {
                networkAddress = InetAddress.getLocalHost().getHostAddress().substring(0, InetAddress.getLocalHost().getHostAddress().lastIndexOf(".") + 1);
            }
        } catch (Exception ex) {
            Error.NETWORK_ADDRESS.record().create(ex);
        }
        for (Integer i = 1; i < 254; i++) {
            new SDEThread(new BuildNetwork(networkAddress, i.toString()), "Network Builder for ip " + i.toString(), null, true);
        }
    }

    static class BuildNetwork extends SDERunnable {
        private String address;
        private String networkAddress;

        BuildNetwork(String networkAddress, String address) {
            this.address = address;
            this.networkAddress = networkAddress;
        }

        public void threadRun() {
            String host = networkAddress + address;
            try {
                String output = SDEUtils.runCMDCommand("ping " + host + " -n 1");
                if (output.contains("Sent = 1, Received = 1") && !output.contains("Destination host unreachable")) {
                    NetworkManager.getInstance().addNetworkNodeInfo(new NetworkNodeInfo(host, InetAddress.getByName(host).getHostName(), true));
                }
            } catch (Exception ex) {
                Error.NETWORK_REACH_HOST.record().additionalInformation("Host " + host).create(ex);
            }
        }
    }
}
