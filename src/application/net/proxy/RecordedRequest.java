package application.net.proxy;

import application.data.DataBank;
import application.data.model.DatabaseObject;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class RecordedRequest extends DatabaseObject {
    private static Logger log = Logger.getLogger(RecordedRequest.class);
    private String URL = "";
    private Integer duration = -1;
    private Integer requestSize = -1;
    private Integer responseSize = -1;
    private String request = "";
    private String response = "";
    private RecordedProxy parentHttpProxy;
    private HashMap<String, RecordedHeader> requestHeaders = new HashMap<>();
    private HashMap<String, RecordedHeader> responseHeaders = new HashMap<>();

    public RecordedRequest() {
        super();
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getBaseURL() {
        if (URL.contains("?")) {
            return URL.substring(0, URL.indexOf("?"));
        } else {
            return URL;
        }
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getRequestSize() {
        return requestSize;
    }

    public void setRequestSize(Integer requestSize) {
        this.requestSize = requestSize;
    }

    public Integer getResponseSize() {
        return responseSize;
    }

    public void setResponseSize(Integer responseSize) {
        this.responseSize = responseSize;
    }

    public String getRequest() {
        if ("".equals(request)) {
            DataBank.loadLazyRequest(this);
        }
        return request;
    }

    public void setRequest(InputStream inputStream) {

    }

    public void setRequest(String request) {
        if (request == null) {
            this.request = "";
        } else {
            this.request = request;
        }
    }

    public void addRecordedHeader(RecordedHeader recordedHeader) {
        if ("request".equals(recordedHeader.getType())) {
            requestHeaders.put(recordedHeader.getName(), recordedHeader);
        } else if ("response".equals(recordedHeader.getType())) {
            responseHeaders.put(recordedHeader.getName(), recordedHeader);
        }
    }

    public void addNewRequestHeader(String name, String value) {
        RecordedHeader requestHeader = RecordedHeader.create(RecordedHeader.class);
        requestHeader.setParent(this);
        requestHeader.setName(name);
        requestHeader.setValue(value);
        requestHeader.setType("request");
        requestHeader.save();
        requestHeaders.put(requestHeader.getName(), requestHeader);
    }

    public void addNewResponseHeader(String name, String value) {
        RecordedHeader responseHeader = RecordedHeader.create(RecordedHeader.class);
        responseHeader.setParent(this);
        responseHeader.setName(name);
        responseHeader.setValue(value);
        responseHeader.setType("response");
        responseHeader.save();
        responseHeaders.put(responseHeader.getName(), responseHeader);
    }

    public void setResponse(InputStream inputStream) {

    }

    public InputStream getRequestInputStream() {
        return new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
    }

    public String getResponse() {
        if ("".equals(response)) {
            DataBank.loadLazyResponse(this);
        }

        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public DateTime getResponseDateTimeFromHeaders() {
        if (responseHeaders.get("Date") != null) {
            DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern(" EEE, dd MMM yyyy HH:mm:ss zzz");
            return dateStringFormat.parseDateTime(responseHeaders.get("Date").getValue());
        }

        return null;
    }

    public InputStream getResponseInputStream() {
        return new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
    }

    public String getParentHttpProxyUuid() {
        if (parentHttpProxy != null) {
            return parentHttpProxy.getUuidString();
        }

        return null;
    }

    public RecordedProxy getParentHttpProxy() {
        return parentHttpProxy;
    }

    public void setParentHttpProxy(RecordedProxy parentHttpProxy) {
        this.parentHttpProxy = parentHttpProxy;
    }

    public HashMap<String, RecordedHeader> getRequestHeaders() {
        return requestHeaders;
    }

    public HashMap<String, RecordedHeader> getResponseHeaders() {
        return responseHeaders;
    }

    public String getProxyConnectionString() {
        if (parentHttpProxy != null) {
            return parentHttpProxy.getConnectionString();
        }
        return "No Proxy";
    }
}
