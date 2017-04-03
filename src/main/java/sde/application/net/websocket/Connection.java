package sde.application.net.websocket;

import sde.application.utils.SDEThread;
import sde.application.utils.managers.WebSocketConnectionManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class Connection implements Runnable {
    private static Logger log = Logger.getLogger(Connection.class);

    private InputStream inputStream = null;

    public Connection(InputStream inputStream) {
        WebSocketConnectionManager.getInstance().addWebSocketServer(this);
        this.inputStream = inputStream;
        SDEThread sdeThread = new SDEThread(this, "WebSocket Connection", "", true);
    }

    @Override
    public void run() {
        boolean inputOpen = true;
        while (inputOpen) {
            IncomingMessage incomingMessage = new IncomingMessage(inputStream);
            inputOpen = incomingMessage.isInputOpen();
            log.info("Out from incoming message: " + incomingMessage.getFinalMessage());
        }
    }

    public void close() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
