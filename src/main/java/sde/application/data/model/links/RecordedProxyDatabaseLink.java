package sde.application.data.model.links;

import sde.application.data.model.DatabaseLink;
import sde.application.net.proxy.RecordedProxy;

public class RecordedProxyDatabaseLink extends DatabaseLink {
    public RecordedProxyDatabaseLink() {
        super("http_proxies", RecordedProxy.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuidFromString", String.class)); // 1
        link("request_count", method("getRequestCount"), method("setRequestCount", Integer.class)); // 2
        link("connection_string", method("getConnectionString"), method("setConnectionString", String.class)); // 3
    }
}
