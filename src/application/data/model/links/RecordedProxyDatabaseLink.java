package application.data.model.links;

import application.data.model.DatabaseLink;
import application.net.proxy.RecordedProxy;

public class RecordedProxyDatabaseLink extends DatabaseLink {
    public RecordedProxyDatabaseLink() {
        super("http_proxies", RecordedProxy.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuidFromString", String.class)); // 1
        link("request_count", method("getRequestCount"), method("setRequestCount", Integer.class)); // 2
    }
}
