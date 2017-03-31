package application.data.model.links;

import application.data.model.DatabaseLink;
import application.net.proxy.RecordedRequest;
import application.test.core.TestCase;

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
        link("protocol_version", method("getProtocolVersion"), method("setProtocolVersion", String.class)); // 9
        link("status", method("getStatus"), method("setStatus", Integer.class)); // 10
        link("statusText", method("getStatusText"), method("setStatusText", String.class)); // 11
        link("test_case_parent_id", method("getParentTestCaseUuid"), method("setParentTestCase", TestCase.class)); // 12
        link("reference", method("getReference"), method("setReference", String.class)); // 13
        link("method", method("getMethod"), method("setMethod", String.class)); // 14
    }
}
