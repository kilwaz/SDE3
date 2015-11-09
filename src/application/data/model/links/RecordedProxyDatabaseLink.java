package application.data.model.links;

import application.data.model.DatabaseLink;
import application.error.Error;
import application.net.proxy.RecordedProxy;

public class RecordedProxyDatabaseLink extends DatabaseLink {
    public RecordedProxyDatabaseLink() {
        super("http_proxies");
        try {
            // Make sure the order is the same as column order in database
            link("request_count", RecordedProxy.class.getMethod("getRequestCount")); // 1
        } catch (NoSuchMethodException ex) {
            Error.DATA_LINK_METHOD_NOT_FOUND.record().create(ex);
        }
    }
}
