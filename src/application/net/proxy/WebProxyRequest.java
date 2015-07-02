package application.net.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.joda.time.Instant;
import org.joda.time.Interval;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
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

    //private HttpRequest httpRequest;
    private Integer requestStatus = REQUEST_STATUS_NOT_STARTED;

    private Instant instantStartProxyToServer;
    private Instant instantCompleteProxyToServer;
    private Instant instantStartServerToProxy;
    private Instant instantCompleteServerToProxy;

    private List<HttpObject> requestHttpObjects = new ArrayList<>();
    private List<HttpObject> responseHttpObjects = new ArrayList<>();

    private Integer requestContentSize = 0;
    private Integer responseContentSize = 0;
    private Integer requestID = 0;
    private String requestContent = "";
    private String responseContent = "";
    private String requestUri = "";
    private ByteBuffer responseBuffer;
    private HashMap<String, String> responseHeaders = new HashMap<>();
    private HashMap<String, String> requestHeaders = new HashMap<>();

    public  WebProxyRequest() {

    }

    public WebProxyRequest(Integer requestID) {
        this.requestID = requestID;
    }

    public WebProxyRequest(HttpRequest httpRequest, Integer requestID) {
        //this.httpRequest = httpRequest;
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

    public void addFullHttpRequest(FullHttpRequest fullHttpRequest) {
        HttpHeaders httpHeaders = fullHttpRequest.headers();
        for (CharSequence headerName : httpHeaders.names()) {
            requestHeaders.put(headerName.toString(), httpHeaders.get(headerName).toString());
        }

        ByteBuf buf = fullHttpRequest.content();
        String content = buf.toString(CharsetUtil.UTF_8);
        requestContentSize += content.length();
        requestContent = content;
    }

    public void addFullHttpResponse(FullHttpResponse fullHttpResponse) {
        HttpHeaders httpHeaders = fullHttpResponse.headers();
        for (CharSequence headerName : httpHeaders.names()) {
            responseHeaders.put(headerName.toString(), httpHeaders.get(headerName).toString());
        }

        ByteBuf buf = fullHttpResponse.content();
        String content = buf.toString(CharsetUtil.UTF_8);
        responseContentSize += content.length();
        responseContent = content;
    }

    public void addRequestHttpObject(HttpObject httpObject) {
        requestHttpObjects.add(httpObject);
    }

    public void addResponseHttpObject(HttpObject httpObject) {
        responseHttpObjects.add(httpObject);
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

    public String getRequestContent() {
        return requestContent;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public String getRequestContentSize() {
        DecimalFormat formatter = new DecimalFormat("###,###");

        if (requestContentSize == 0) {
            return "0";
        } else {
            return formatter.format(requestContentSize) + " bytes";
        }
    }

    public Integer getResponseContentSize() {
        return responseContentSize;
    }

    public ByteBuffer getResponseBuffer() {
        return responseBuffer;
    }

    public void setResponseBuffer(ByteBuffer responseBuffer) {
        this.responseBuffer = responseBuffer;
        this.requestContentSize = responseBuffer.limit();
    }

    public void setRequestID(Integer requestID) {
        this.requestID = requestID;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }
}
