package application.utils;

import application.data.NetworkNodeInfo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;

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
        try{
            if(InetAddress.getLocalHost().getHostAddress() != null){
                networkAddress = InetAddress.getLocalHost().getHostAddress().substring(0,InetAddress.getLocalHost().getHostAddress().lastIndexOf(".")+1);
            }
        }catch (Exception ex){
            System.out.println("Exception occurred while trying to retrieve networkAddress stack: " + ex);
        }
        for (Integer i = 1; i < 254; i++) {
            Thread thread = new Thread(new buildNetwork(i.toString()));
            thread.start();
        }
    }

    class buildNetwork implements Runnable {
        private String address;

        buildNetwork(String address) {
            this.address = address;
        }

        public void run() {
            String host = networkAddress + address;
            try {
               if (InetAddress.getByName(host).isReachable(1100)) {
                   //System.out.println(host + " is reachable. Using Host Name: " + InetAddress.getByName(host).getHostName());
                   NetworkManager.getInstance().addNetworkNodeInfo(new NetworkNodeInfo(host,InetAddress.getByName(host).getHostName(),true));
               }
            } catch (Exception ex) {
                System.out.println("Exception occurred while trying to reach host " + host + " stack: " + ex);
            }
        }
    }
}
