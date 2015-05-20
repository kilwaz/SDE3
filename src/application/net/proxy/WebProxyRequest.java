package application.net.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import org.joda.time.Instant;
import org.joda.time.Interval;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class WebProxyRequest {

    public static final Integer REQUEST_STATUS_NOT_STARTED = 0;
    public static final Integer REQUEST_STATUS_CLIENT_TO_PROXY = 1;
    public static final Integer REQUEST_STATUS_PROXY_TO_SERVER = 2;
    public static final Integer REQUEST_STATUS_SERVER_TO_PROXY = 3;
    public static final Integer REQUEST_STATUS_PROXY_TO_CLIENT = 4;
    public static final Integer REQUEST_STATUS_COMPLETED = 5;

    private HttpRequest httpRequest;
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

    public WebProxyRequest(HttpRequest httpRequest, Integer requestID) {
        this.httpRequest = httpRequest;
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

    public void addRequestHttpObject(HttpObject httpObject) {
        if (httpObject instanceof DefaultHttpContent) {
            DefaultHttpContent response = (DefaultHttpContent) httpObject;
            ByteBuf buf = response.content();
            //ByteBuf newBuf = Unpooled.wrappedBuffer(buf);
            String originalContent = buf.toString(CharsetUtil.UTF_8);
            requestContentSize += originalContent.length();
            //System.out.println("1 Size? " + originalContent.length());
            //System.out.println(originalContent);
        } else if (httpObject instanceof LastHttpContent) {
            LastHttpContent response = (LastHttpContent) httpObject;
            ByteBuf buf = response.content();
            //ByteBuf newBuf = Unpooled.wrappedBuffer(buf);
            String originalContent = buf.toString(CharsetUtil.UTF_8);
            requestContentSize += originalContent.length();
            //System.out.println("2 Size? " + originalContent.length());
            //System.out.println(originalContent);
        }

        requestHttpObjects.add(httpObject);
    }

    public void addResponseHttpObject(HttpObject httpObject) {
        if (httpObject instanceof DefaultHttpContent) {
            DefaultHttpContent response = (DefaultHttpContent) httpObject;
            ByteBuf buf = response.content();
            //ByteBuf newBuf = Unpooled.wrappedBuffer(buf);
            String originalContent = buf.toString(CharsetUtil.UTF_8);
            responseContentSize += originalContent.length();
            //System.out.println("OUT 1 Size? " + originalContent.length());
            //System.out.println(originalContent);
        } else if (httpObject instanceof LastHttpContent) {
            LastHttpContent response = (LastHttpContent) httpObject;
            ByteBuf buf = response.content();
            //ByteBuf newBuf = Unpooled.wrappedBuffer(buf);
            String originalContent = buf.toString(CharsetUtil.UTF_8);
            responseContentSize += originalContent.length();
            //System.out.println("OUT 2 Size? " + originalContent.length());
            //System.out.println(originalContent);
        }

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
        return httpRequest.getUri();
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
}
