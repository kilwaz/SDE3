package sde.application.test.selenium;

import org.apache.log4j.Logger;
import sde.application.utils.SDERunnable;
import sde.application.utils.SDEThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeHelperSocketServer extends SDERunnable {

    private static Logger log = Logger.getLogger(NodeHelperSocketServer.class);

    private Integer listenPortNumber = 4446;

    public NodeHelperSocketServer() {

    }

    public static void execute() {
        new SDEThread(new NodeHelperSocketServer(), "Selenium node helper listener server", "", true);
    }

    public void threadRun() {
        try {
            ServerSocket serverSocket = new ServerSocket(listenPortNumber);
            log.info("Listening for selenium node helper commands...");
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine, outputLine;

            while ((inputLine = in.readLine()) != null) {
                NodeHelperMessageDecoder nodeHelperMessageDecoder = new NodeHelperMessageDecoder(inputLine);

                if (nodeHelperMessageDecoder.hasResponse()) {
                    out.println(nodeHelperMessageDecoder.getCurrentResponse());
                }

                if (nodeHelperMessageDecoder.isGoodBye()) {
                    break;
                }
            }
            log.info("Listener closing down");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
