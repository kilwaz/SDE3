package application.data.model.links;

import application.data.model.DatabaseLink;
import application.net.proxy.RecordedRequest;

import java.io.InputStream;
import java.util.UUID;

public class RecordedRequestDatabaseLink extends DatabaseLink {
    public RecordedRequestDatabaseLink() {
        super("recorded_requests", RecordedRequest.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("http_proxy_id", method("getParentHttpProxyUuid"), null); // 2
        link("url", method("getUrl"), method("setUrl", String.class)); // 3
        link("duration", method("getDuration"), method("setDuration", Integer.class)); // 4
        link("request_size", method("getRequestSize"), method("setRequestSize", Integer.class)); // 5
        link("response_size", method("getResponseSize"), method("setResponseSize", Integer.class)); // 6
        linkBlob("request_content", method("getRequestInputStream"), null); // 7
        linkBlob("response_content", method("getResponseInputStream"), null); // 8

//        link("request_content", method("getRequest"), null); // 7 - lazy load, only found if needed
//        link("response_content", method("getResponse"), null); // 8 - lazy load, only found if needed
    }
}
