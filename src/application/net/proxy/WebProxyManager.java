package application.net.proxy;

import application.net.proxy.snoop.HttpProxyServer;

import java.util.ArrayList;
import java.util.List;

public class WebProxyManager {
    private static WebProxyManager webProxyManager;
    private List<HttpProxyServer> openProxies;

    public WebProxyManager() {
        webProxyManager = this;
        openProxies = new ArrayList<>();
    }

    public void addConnection(HttpProxyServer webProxy) {
        openProxies.add(webProxy);
    }

    public void closeProxies() {
        openProxies.forEach(HttpProxyServer::close);
    }

    public static WebProxyManager getInstance() {
        return webProxyManager;
    }
}
