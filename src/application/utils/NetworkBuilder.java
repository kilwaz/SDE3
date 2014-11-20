package application.utils;

import application.data.NetworkNodeInfo;

import java.net.InetAddress;

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 20/10/14
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */
public class NetworkBuilder {
    public static String networkAddress = "0.0.0.";

    public NetworkBuilder() {
        try {
            if (InetAddress.getLocalHost().getHostAddress() != null) {
                networkAddress = InetAddress.getLocalHost().getHostAddress().substring(0, InetAddress.getLocalHost().getHostAddress().lastIndexOf(".") + 1);
            }
        } catch (Exception ex) {
            System.out.println("Exception occurred while trying to retrieve networkAddress stack: " + ex);
        }
        for (Integer i = 1; i < 254; i++) {
            new SDEThread(new BuildNetwork(i.toString()));
        }
    }

    class BuildNetwork extends SDERunnable {
        private String address;

        BuildNetwork(String address) {
            this.address = address;
        }

        public void threadRun() {
            String host = networkAddress + address;
            try {
                String output = SDEUtils.runCMDCommand("ping " + host + " -n 1");
                if (output.contains("Sent = 1, Received = 1") && !output.contains("Destination host unreachable")) {
                    NetworkManager.getInstance().addNetworkNodeInfo(new NetworkNodeInfo(host, InetAddress.getByName(host).getHostName(), true));
                }
            } catch (Exception ex) {
                System.out.println("Exception occurred while trying to reach host " + host + " stack: " + ex);
            }
        }
    }
}
