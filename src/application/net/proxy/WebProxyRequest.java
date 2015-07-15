package application.net.proxy;

import io.netty.handler.codec.http.HttpObject;
import org.joda.time.Instant;
import org.joda.time.Interval;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WebProxyRequest {

    public static final Integer REQUEST_STATUS_NOT_STARTED = 0;
    public static final Integer REQUEST_STATUS_CLIENT_TO_PROXY = 1;
    public static final Integer REQUEST_STATUS_PROXY_TO_SERVER = 2;
    public static final Integer REQUEST_STATUS_SERVER_TO_PROXY = 3;
    public static final Integer REQUEST_STATUS_PROXY_TO_CLIENT = 4;
    public static final Integer REQUEST_STATUS_COMPLETED = 5;

    private Integer requestStatus = REQUEST_STATUS_NOT_STARTED;

    private Instant instantStartProxyToServer;
    private Instant instantCompleteProxyToServer;
    private Instant instantStartServerToProxy;
    private Instant instantCompleteServerToProxy;

    private List<HttpObject> requestHttpObjects = new ArrayList<>();
    private List<HttpObject> responseHttpObjects = new ArrayList<>();

    private Integer responseContentSize = 0;
    private Integer requestID = 0;
    private String requestContent = "";
    private String requestUri = "";
    private ByteBuffer responseBuffer;
    private HashMap<String, String> responseHeaders = new HashMap<>();
    private HashMap<String, String> requestHeaders = new HashMap<>();

    public WebProxyRequest() {

    }

    public WebProxyRequest(Integer requestID) {
        this.requestID = requestID;
    }

    public void instantStartProxyToServer() {
        instantStartProxyToServer = new Instant();
    }

    public void instantCompleteProxyToServer() {
        instantCompleteProxyToServer = new Instant();
    }

    public void instantStartServerToProxy() {
        instantStartServerToProxy = new Instant();
    }

    public void instantCompleteServerToProxy() {
        instantCompleteServerToProxy = new Instant();
    }

    public Integer getRequestHttpObjectCount() {
        return requestHttpObjects.size();
    }

    public Integer getResponseHttpObjectCount() {
        return responseHttpObjects.size();
    }

    public void setRequestStatus(Integer requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getRequestURL() {
        return requestUri;
    }

    public Long getRequestDuration() {
        if (instantStartProxyToServer != null && instantCompleteServerToProxy != null) {
            Interval interval = new Interval(instantStartProxyToServer, instantCompleteServerToProxy);
            return interval.toDuration().getMillis();
        }

        return -1l;
    }

    public Integer getRequestID() {
        return requestID;
    }

    public HashMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public HashMap<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setRequestHeaders(HashMap<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setResponseHeaders(HashMap<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getRequestContent() {
        return requestContent;
    }

    public void setRequestContent(String requestContent) {
        this.requestContent = requestContent;
    }

    public String getResponseContent() {
        return new String(responseBuffer.array(), Charset.forName("UTF-8"));
    }

    public Integer getRequestContentSize() {
        return requestContent.length();
    }

    public Integer getResponseContentSize() {
        return responseContentSize;
    }

    public ByteBuffer getResponseBuffer() {
        return responseBuffer;
    }

    public void setResponseBuffer(ByteBuffer responseBuffer) {
        this.responseBuffer = responseBuffer;
        this.responseContentSize = responseBuffer.limit();
    }

    public void setRequestID(Integer requestID) {
        this.requestID = requestID;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }
}
