package sde.application.utils.managers;

import sde.application.net.websocket.Listener;

import java.util.ArrayList;
import java.util.List;

public class WebSocketListenerManager {
    private static WebSocketListenerManager instance;
    private static List<Listener> webSocketListeners = new ArrayList<>();

    public WebSocketListenerManager() {
        instance = this;
    }

    public static WebSocketListenerManager getInstance() {
        if (instance == null) {
            instance = new WebSocketListenerManager();
        }
        return instance;
    }

    public void addWebSocketServer(Listener listener) {
        webSocketListeners.add(listener);
    }

    public void closeAllWebSocketListeners() {
        webSocketListeners.forEach(Listener::close);
    }
}
