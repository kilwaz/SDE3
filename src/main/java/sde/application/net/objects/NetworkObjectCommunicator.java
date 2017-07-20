package sde.application.net.objects;

import com.jayway.awaitility.Awaitility;
import org.apache.log4j.Logger;
import sde.application.error.Error;
import sde.application.net.objects.messages.client.Command;
import sde.application.net.objects.messages.client.DeleteRecording;
import sde.application.net.objects.messages.initilise.ClientHello;
import sde.application.net.objects.messages.initilise.ServerHello;
import sde.application.net.objects.messages.server.Recording;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class NetworkObjectCommunicator {
    private static Logger log = Logger.getLogger(NetworkObjectCommunicator.class);

    private Boolean initialised = false;

    private Socket socket;
    private ObjectOutputStream oos = null;
    private ObjectInputStream ois = null;
    private Boolean isServer = false;

    public NetworkObjectCommunicator(Socket socket) {
        this.socket = socket;
    }

    public NetworkObjectCommunicator isSever(Boolean isServer) {
        this.isServer = isServer;
        return this;
    }

    public void execute() {
        log.info("Executing Network Object Communicator");
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());

            if (!isServer) {
                sendClientHello();
            }

            NetworkObject networkObject;

            log.info("Waiting for objects..");

            ois = new ObjectInputStream(socket.getInputStream());
            while ((networkObject = (NetworkObject) ois.readObject()) != null) {
                log.info("Got: " + networkObject.getName());

                // Initialise the connection both ends
                if (networkObject instanceof ServerHello || networkObject instanceof ClientHello) {
                    log.info("Connection ready to be used");
                    this.initialised = true;
                } else if (networkObject instanceof Command) {
                    Command command = (Command) networkObject;
                    command.runCommand();
                } else if (networkObject instanceof Recording) {
                    Recording recording = (Recording) networkObject;
                    recording.saveFile();
                } else if (networkObject instanceof DeleteRecording) {
                    DeleteRecording deleteRecording = (DeleteRecording) networkObject;
                    deleteRecording.deleteFile();
                }

                // Send any commands back if any are generated
                for (NetworkObject responseObject : networkObject.getResponses()) {
                    sendNetworkObject(responseObject);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendClientHello() {
        sendNetworkObject(new ClientHello());
    }

    public void sendNetworkObject(NetworkObject networkObject) {
        try {
            // Let client hello go through a non inited connection, everything else needs to wait
            if (!(networkObject instanceof ClientHello)) {
                Awaitility.await().atMost(10, TimeUnit.SECONDS).until(nowConnected());
            }
            oos.writeObject(networkObject);
        } catch (IOException ex) {
            Error.SEND_NETWORK_OBJECT_FAILED.record().create(ex);
        }
    }

    public Callable<Boolean> nowConnected() {
        return () -> {
            return initialised; // The condition that must be fulfilled
        };
    }
}
