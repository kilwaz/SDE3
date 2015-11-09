package application.data.model.links;

import application.data.model.DatabaseLink;
import application.error.Error;
import application.net.proxy.RecordedRequest;

public class RecordedRequestDatabaseLink extends DatabaseLink {
    public RecordedRequestDatabaseLink() {
        super("recorded_requests");
        try {
            // Make sure the order is the same as column order in database
            link("http_proxy_id", RecordedRequest.class.getMethod("getParentHttpProxyId")); // 1
            link("url", RecordedRequest.class.getMethod("getURL")); // 2
            link("duration", RecordedRequest.class.getMethod("getDuration")); // 3
            link("request_size", RecordedRequest.class.getMethod("getRequestSize")); // 4
            link("response_size", RecordedRequest.class.getMethod("getResponseSize")); // 5
            linkBlob("request_content", RecordedRequest.class.getMethod("getRequestInputStream")); // 6
            linkBlob("response_content", RecordedRequest.class.getMethod("getResponseInputStream")); // 7
        } catch (NoSuchMethodException ex) {
            Error.DATA_LINK_METHOD_NOT_FOUND.record().create(ex);
        }
    }
}
