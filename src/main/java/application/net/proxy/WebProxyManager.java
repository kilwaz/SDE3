package application.net.proxy;

import application.net.proxy.snoop.HttpProxyServer;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WebProxyManager {
    private static WebProxyManager webProxyManager;
    private List<HttpProxyServer> openProxies;

    public WebProxyManager() {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_NONE));
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
        // We make a copy of the list before closing the proxies so that we don't get any list concurrency errors
        List<HttpProxyServer> proxiesToClose = new ArrayList<>();
        proxiesToClose.addAll(openProxies);
        proxiesToClose.forEach(HttpProxyServer::close);
    }
}
