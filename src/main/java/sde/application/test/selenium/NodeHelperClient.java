package sde.application.test.selenium;

import com.jayway.awaitility.Awaitility;
import org.apache.log4j.Logger;
import sde.application.net.objects.NetworkObjectCommunicator;
import sde.application.net.objects.messages.client.Command;
import sde.application.net.objects.messages.client.RetrieveRecording;
import sde.application.net.proxy.snoop.SSLContextProvider;
import sde.application.utils.SDERunnable;
import sde.application.utils.SDEThread;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class NodeHelperClient extends SDERunnable {
    private static Logger log = Logger.getLogger(NodeHelperClient.class);
    private Socket socket;
    private Boolean useSSL = false;

    private String host = "";
    private Integer serverPort = 4446;

    private NetworkObjectCommunicator networkObjectCommunicator = null;

    public NodeHelperClient() {
    }

    public NodeHelperClient useSSL(Boolean useSSL) {
        this.useSSL = useSSL;
        return this;
    }

    public NodeHelperClient host(String host) {
        this.host = host;
        return this;
    }

    public void execute() {
        new SDEThread(this, "Selenium node helper client communication", "", true);
    }

    public void threadRun() {
        try {
            socket = createSocket();
            //getSSLSession(socket);

            networkObjectCommunicator = new NetworkObjectCommunicator(socket);
            // This won't return until the connection is closed
            networkObjectCommunicator.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getSSLSession(Socket socket) throws SSLPeerUnverifiedException {
        if (useSSL) {
            SSLSession session = ((SSLSocket) socket).getSession();
            Certificate[] cchain = session.getPeerCertificates();
            log.info("The Certificates used by peer");
            for (int i = 0; i < cchain.length; i++) {
                log.info(((X509Certificate) cchain[i]).getSubjectDN());
            }
            log.info("Peer host is " + session.getPeerHost());
            log.info("Cipher is " + session.getCipherSuite());
            log.info("Protocol is " + session.getProtocol());
            log.info("ID is " + new BigInteger(session.getId()));
            log.info("Session created in " + session.getCreationTime());
            log.info("Session accessed in " + session.getLastAccessedTime());
        }
    }

    private Socket createSocket() throws IOException {
        if (useSSL) {
            SSLSocketFactory ssf = SSLContextProvider.get().getSocketFactory();
            return ssf.createSocket(host, serverPort);
        } else {
            return new Socket(host, serverPort);
        }
    }

    public void waitForEstablishedConnection() {
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(establishedConnected());
    }

    private Callable<Boolean> establishedConnected() {
        return () -> {
            return networkObjectCommunicator != null; // The condition that must be fulfilled
        };
    }

    public void startRecording(String recordingReference, String screenNumber) {
        log.info("Sending start recording command " + networkObjectCommunicator);
        Command command = new Command().setCommand("tmux new-session -d -s " + recordingReference + " 'ffmpeg -f x11grab -video_size 1920x1080 -i :" + screenNumber + " -codec:v libx264 -r 12 /home/spiralinks/" + recordingReference + ".mp4'");
        waitForEstablishedConnection();
        networkObjectCommunicator.sendNetworkObject(command);
    }

    public void endRecording(String recordingReference) {
        log.info("Sending end recording command");
        Command command = new Command().setCommand("tmux send-keys -t " + recordingReference + " q");
        waitForEstablishedConnection();
        networkObjectCommunicator.sendNetworkObject(command);
    }

    public void retrieveRecording(String recordingReference, String localFileLocation) {
        RetrieveRecording retrieveRecording = new RetrieveRecording()
                .setRecordingReference("/home/spiralinks/" + recordingReference + ".mp4")
                .setLocalFileLocation(localFileLocation);
        waitForEstablishedConnection();
        networkObjectCommunicator.sendNetworkObject(retrieveRecording);
    }
}
