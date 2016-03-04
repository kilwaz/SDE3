package application.net.proxy;

import application.net.proxy.snoop.HttpProxyServer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WebProxyManager {
    private static WebProxyManager webProxyManager;
    private List<HttpProxyServer> openProxies;

    public WebProxyManager() {
        webProxyManager = this;
        openProxies = new ArrayList<>();
    }

    public static WebProxyManager getInstance() {
        return webProxyManager;
    }

    public void addConnection(HttpProxyServer webProxy) {
        openProxies.add(webProxy);
    }

    public List<HttpProxyServer> getOpenProxies() {
        return openProxies;
    }

    public void removeInactiveProxies() {
        List<HttpProxyServer> proxiesToRemove = openProxies.stream().filter(proxy -> proxy.getStatus().equals(HttpProxyServer.STATUS_CLOSED)).collect(Collectors.toList());
        openProxies.removeAll(proxiesToRemove);
    }

    public void closeProxies() {
        openProxies.forEach(HttpProxyServer::close);
    }
}
