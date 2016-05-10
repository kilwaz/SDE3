package application.net.proxy;

import application.error.Error;
import io.netty.handler.codec.http.HttpObject;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

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

    public String getResponseContent() {
        if (responseBuffer != null) {
            if ("gzip".equals(getResponseHeader("Content-Encoding"))) { // If the response is encoded as a gzip, decompress into a string
                ByteBufferBackedInputStream in = new ByteBufferBackedInputStream(responseBuffer);
                InputStream zin = null;
                try {
                    zin = new GZIPInputStream(in);
                    return IOUtils.toString(zin, Charset.forName("UTF-8"));
                } catch (IOException ex) {
                    Error.FAILED_TO_DECODE_GZIP_RESPONSE.record().create(ex);
                } finally {
                    try {
                        if (zin != null) {
                            zin.close();
                        }
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                return new String(responseBuffer.array(), Charset.forName("UTF-8"));
            }
        }

        return "No Response Data";
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
