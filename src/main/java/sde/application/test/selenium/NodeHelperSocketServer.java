package sde.application.test.selenium;

import org.apache.log4j.Logger;
import sde.application.net.objects.NetworkObjectCommunicator;
import sde.application.net.proxy.snoop.SSLContextProvider;
import sde.application.utils.SDERunnable;
import sde.application.utils.SDEThread;

import javax.net.ServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeHelperSocketServer extends SDERunnable {

    private static Logger log = Logger.getLogger(NodeHelperSocketServer.class);

    private Integer listenPortNumber = 4446;
    private Boolean useSSL = false;

    public NodeHelperSocketServer() {

    }

    public NodeHelperSocketServer useSSL() {
        log.info("Using SSL");
        this.useSSL = true;
        return this;
    }

    public void execute() {
        new SDEThread(this, "Selenium node helper listener server", "", true);
    }

    public void threadRun() {
        try {
            log.info("Creating socket server with SSL = " + useSSL);
            ServerSocket serverSocket = initServer(); // Create the server

            log.info("Listening for selenium node helper commands... (port " + listenPortNumber + ")");
            Socket clientSocket = listenForNewConnection(serverSocket); // Listen for a new connection

            log.info("Incoming connection");

            NetworkObjectCommunicator networkObjectCommunicator = new NetworkObjectCommunicator(clientSocket).isSever(true);
            // This won't return until the connection is closed
            networkObjectCommunicator.execute();

            log.info("Listener closing down");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket listenForNewConnection(ServerSocket serverSocket) throws IOException {
        return serverSocket.accept();
    }

    private ServerSocket initServer() throws IOException {
        if (useSSL) { // KeyStore Socket
            ServerSocketFactory serverSocketFactory = SSLContextProvider.get().getServerSocketFactory();

            ServerSocket serverSocket = serverSocketFactory.createServerSocket(listenPortNumber);

            return serverSocket;
        } else { // Non KeyStore Socket
            ServerSocket serverSocket = new ServerSocket(listenPortNumber);

            return serverSocket;
        }
    }

    private void runCommand(String command) {
        log.info("Running command ... " + command);

        StringBuffer output = new StringBuffer();

        String[] args = new String[]{"/bin/bash", "-c", command};
        Process p = null;
        try {
            p = new ProcessBuilder(args).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (p != null) {
                p.waitFor();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    output.append(line + "\n");
                }
                log.info("Completed command");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
