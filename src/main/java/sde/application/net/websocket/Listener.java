package sde.application.net.websocket;

import sde.application.error.Error;
import sde.application.utils.managers.WebSocketListenerManager;
import org.apache.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Listener implements Runnable {
    private static Logger log = Logger.getLogger(Listener.class);

    private static int port = 1122;
    private InputStream inputStream = null;
    private ServerSocket serverSocket = null;

    public Listener() {
        WebSocketListenerManager.getInstance().addWebSocketServer(this);
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            log.info("Listener started, waiting for a client");
        } catch (IOException ex) {
            Error.WEBSOCKET_SERVER_STARTING.record().create(ex);
        }

        try {
            if (serverSocket != null) {
                Boolean acceptConnections = true;
                while (acceptConnections) {
                    Socket client = null;
                    try {
                        client = serverSocket.accept();
                    } catch (SocketException ex) {
                        log.info("Shutting down WebSocket listener");
                        acceptConnections = false;
                        continue;
                    }
                    log.info("Listener started, waiting for a client");

                    inputStream = client.getInputStream();
                    OutputStream out = client.getOutputStream();

                    //translate bytes of request to string
                    Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\r\\n\\r\\n");
                    String data = scanner.next();
                    Matcher get = Pattern.compile("^GET").matcher(data);

                    if (get.find()) {
                        Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                        match.find();
                        byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                                + "Connection: Upgrade\r\n"
                                + "Upgrade: websocket\r\n"
                                + "Sec-WebSocket-Accept: "
                                + DatatypeConverter
                                .printBase64Binary(
                                        MessageDigest
                                                .getInstance("SHA-1")
                                                .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                                        .getBytes("UTF-8")))
                                + "\r\n\r\n")
                                .getBytes("UTF-8");

                        out.write(response, 0, response.length);
                        log.info("Handshake response written");
                    } else {
                        log.info("Something else happened");
                    }

                    // The Connection object will manage the web socket connection from this point on
                    new Connection(inputStream);
                }
            }
        } catch (IOException ex) {
            Error.WEBSOCKET_SERVER_STARTING.record().create(ex);
        } catch (NoSuchAlgorithmException ex) {
            Error.WEBSOCKET_SERVER_NO_SUCH_ALGORITHM.record().create(ex);
        }
    }

    public void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException ex) {
            Error.WEBSOCKET_CANNOT_CLOSE_INPUT.record().create(ex);
        }
    }
}
