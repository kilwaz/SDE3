package sde.application.utils.managers;

import sde.application.net.websocket.Connection;

import java.util.ArrayList;
import java.util.List;

public class WebSocketConnectionManager {
    private static WebSocketConnectionManager instance;
    private static List<Connection> webSocketConnections = new ArrayList<>();

    public WebSocketConnectionManager() {
        instance = this;
    }

    public static WebSocketConnectionManager getInstance() {
        if (instance == null) {
            instance = new WebSocketConnectionManager();
        }
        return instance;
    }

    public void addWebSocketServer(Connection connection) {
        webSocketConnections.add(connection);
    }

    public void closeAllWebSocketConnections() {
        webSocketConnections.forEach(Connection::close);
    }
}
