package sde.application.test.selenium;

import sde.application.utils.SDERunnable;
import sde.application.utils.SDEThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NodeHelperClient extends SDERunnable {
    public NodeHelperClient() {

    }

    public static void execute() {
        new SDEThread(new NodeHelperClient(), "Selenium node helper client communication", "", true);
    }

    public void threadRun() {
        try {
            Socket socket = new Socket("selenium-browser-1", 4446);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String fromServer;

            out.println("GREETINGS");

            while ((fromServer = in.readLine()) != null) {
                NodeHelperMessageDecoder nodeHelperMessageDecoder = new NodeHelperMessageDecoder(fromServer);

                if (nodeHelperMessageDecoder.hasResponse()) {
                    out.println(nodeHelperMessageDecoder.getCurrentResponse());
                }

                if (nodeHelperMessageDecoder.isGoodBye()) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
