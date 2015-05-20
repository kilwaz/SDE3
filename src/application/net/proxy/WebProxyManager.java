package application.net.proxy;

import java.util.ArrayList;
import java.util.List;

public class WebProxyManager {
    private static WebProxyManager webProxyManager;
    private List<WebProxy> openProxies;

    public WebProxyManager() {
        webProxyManager = this;
        openProxies = new ArrayList<>();
    }

    public void addConnection(WebProxy webProxy) {
        openProxies.add(webProxy);
    }

    public void closeProxies() {
        openProxies.forEach(WebProxy::close);
    }

    public static WebProxyManager getInstance() {
        return webProxyManager;
    }
}
