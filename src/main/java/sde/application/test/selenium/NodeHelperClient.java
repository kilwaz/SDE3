package sde.application.test.selenium;

import com.jayway.awaitility.Awaitility;
import org.apache.log4j.Logger;
import sde.application.utils.SDERunnable;
import sde.application.utils.SDEThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class NodeHelperClient extends SDERunnable {
    private static Logger log = Logger.getLogger(NodeHelperClient.class);
    private PrintWriter out = null;
    private Socket socket;
    private Boolean initialised = false;

    private String host = "";
    private Integer serverPort = 4446;

    public NodeHelperClient(String host) {
        this.host = host;
    }

    public void execute() {
        new SDEThread(this, "Selenium node helper client communication", "", true);
    }

    public void threadRun() {
        try {
            socket = new Socket(host, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String fromServer;

            out.println("ClientHello");

            while ((fromServer = in.readLine()) != null) {
                NodeHelperMessageDecoder nodeHelperMessageDecoder = new NodeHelperMessageDecoder(fromServer);

                if (nodeHelperMessageDecoder.isInitialiseFromServer()) {
                    log.info("Setting init as true");
                    initialised = true;
                }

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

    public void startRecordCommand(String recordingName, String screenNumber) {
        sendCommand("COMMANDtmux new-session -d -s " + recordingName + " 'ffmpeg -f x11grab -video_size 1920x1080 -i :" + screenNumber + " -codec:v libx264 -r 12 /home/spiralinks/" + recordingName + ".mp4'");
    }

    public void endRecordCommand(String recordingName) {
        sendCommand("COMMANDtmux send-keys -t " + recordingName + " q");
    }

    public void testCommand() {
        sendCommand("ping 172.16.10.208");
    }

    private void sendCommand(String command) {
        // If not connected wait until we are
        log.info("Initialised is " + this.initialised);
        log.info("Sending " + command);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(nowConnected());

        out.println(command);
    }

    private Boolean isInitialised() {
        return this.initialised;
    }

    private Callable<Boolean> nowConnected() {
        return () -> {
            log.info("Checking inited - " + initialised);
            return initialised; // The condition that must be fulfilled
        };
    }
}
