package sde.application.net.proxy;

import io.netty.handler.codec.http.HttpObject;
import org.apache.http.cookie.Cookie;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.ByteBuffer;
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
    private static Logger log = Logger.getLogger(WebProxyRequest.class);
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
    private Boolean isHttps = false;
    private String method = "";
    private Integer status = -1;
    private String statusText = "";
    private String protocolVersion = "";
    private String redirectUrl = "";
    private Long waitTimeToFirstByte = -1L;
    private Long downloadTime = -1L;
    private HashMap<String, String> responseHeaders = new HashMap<>();
    private HashMap<String, String> requestHeaders = new HashMap<>();
    private List<Cookie> cookies = new ArrayList<>();

    public WebProxyRequest() {

    }

    public WebProxyRequest(Integer requestID) {
        this.requestID = requestID;
    }

    public void instantStartProxyToServer() {
        instantStartProxyToServer = new Instant();
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
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

    public Long getWaitTimeToFirstByte() {
        return waitTimeToFirstByte;
    }

    public void setWaitTimeToFirstByte(Long waitTimeToFirstByte) {
        this.waitTimeToFirstByte = waitTimeToFirstByte;
    }

    public Long getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(Long downloadTime) {
        this.downloadTime = downloadTime;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
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

    public Boolean getHttps() {
        return isHttps;
    }

    public void setHttps(Boolean https) {
        isHttps = https;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Long getRequestDuration() {
        if (instantStartProxyToServer != null && instantCompleteServerToProxy != null) {
            Interval interval = new Interval(instantStartProxyToServer, instantCompleteServerToProxy);
            return interval.toDuration().getMillis();
        }

        return -1l;
    }

    public Instant getInstantStartProxyToServer() {
        return instantStartProxyToServer;
    }

    public Integer getRequestID() {
        return requestID;
    }

    public void setRequestID(Integer requestID) {
        this.requestID = requestID;
    }

    public HashMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(HashMap<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestHeader(String headerName) {
        return requestHeaders.get(headerName);
    }

    public String getResponseHeader(String headerName) {
        return responseHeaders.get(headerName);
    }

    public HashMap<String, String> getResponseHeaders() {
        return responseHeaders;
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

    public Integer getRequestContentSize() {
        return requestContent.length();
    }

    public Integer getResponseContentSize() {
        return responseContentSize;
    }

    public ByteBuffer getResponseBuffer() {
        return this.responseBuffer;
    }

    public void setResponseBuffer(ByteBuffer responseBuffer) {
        this.responseBuffer = responseBuffer;
        if (responseBuffer != null) {
            this.responseContentSize = responseBuffer.limit();
        } else {
            this.responseContentSize = 0;
        }
    }

    public DateTime getResponseDateTimeFromHeers() {
        if (responseHeaders.get("Date") != null) {
            DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern(" EEE, dd MMM yyyy HH:mm:ss zzz");
            return dateStringFormat.parseDateTime(responseHeaders.get("Date"));
        }

        return null;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }
}
