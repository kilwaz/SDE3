package application.net.proxy;

import application.data.model.DatabaseObject;
import application.data.model.dao.RecordedRequestDAO;
import application.test.core.TestCase;
import org.apache.http.cookie.Cookie;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecordedRequest extends DatabaseObject {
    private static Logger log = Logger.getLogger(RecordedRequest.class);
    private String url = "";
    private Integer duration = -1;
    private Integer requestSize = -1;
    private Integer responseSize = -1;
    private String request = "";
    private String response = "";
    private RecordedProxy parentHttpProxy;
    private List<RecordedHeader> requestHeaders = new ArrayList<>();
    private List<RecordedHeader> responseHeaders = new ArrayList<>();
    private Integer requestNumber = 0;
    private String host = "";
    private String redirectUrl = "";
    private Boolean isHttps = false;
    private String method = "";
    private Integer status = -1;
    private String statusText = "";
    private TestCase parentTestCase;
    private String reference;
    private DateTime proxyRequestReceivedTime = null;
    private Long waitTimeToFirstByte = -1L;
    private Long downloadTime = -1L;
    private String protocolVersion = "";
    private List<Cookie> cookies = new ArrayList<>();

    public RecordedRequest() {
        super();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setParentTestCase(TestCase parentTestCase) {
        this.parentTestCase = parentTestCase;
    }

    public DateTime getProxyRequestReceivedTime() {
        return proxyRequestReceivedTime;
    }

    public void setProxyRequestReceivedTime(DateTime proxyRequestReceivedTime) {
        this.proxyRequestReceivedTime = proxyRequestReceivedTime;
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

    public String getParentTestCaseUuid() {
        if (parentTestCase != null) {
            return parentTestCase.getUuidString();
        }
        return null;
    }

    public String getLocalUrl() {
        if (url.contains(":")) {
            String localUrl = url.substring(url.indexOf(":") + 3);
            if (localUrl.contains("/")) {
                return localUrl.substring(localUrl.indexOf("/"));
            }
        }
        return "";
    }

    public String getMediaType() {
        RecordedHeader mediaTypeHeader = getResponseHeader("Content-Type");
        if (mediaTypeHeader != null) {
            String content = mediaTypeHeader.getValue();
            if (content.contains(";")) {
                content = content.substring(0, content.indexOf(";"));
            }
            if (content.contains("/")) {
                content = content.substring(content.indexOf("/") + 1);
                if (content.contains("-")) {
                    return content.substring(content.indexOf("-") + 1);
                } else {
                    return content;
                }
            }
            return content;
        }

        return "";
    }

    public String getExtension() {
        String baseUrl = getBaseUrl();
        if (baseUrl.contains("/")) {
            String page = baseUrl.substring(baseUrl.lastIndexOf("/"));
            if (page.contains(".")) {
                return page.substring(page.lastIndexOf(".") + 1);
            }
        }
        return "";
    }

    public String getTitle() {
        if ("html".equals(getMediaType())) {
            return Jsoup.parse(response).title();
        } else {
            return "";
        }
    }

    public String getRedirectHost() {
        return parseHost(redirectUrl);
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    public List<RecordedHeader> getRequestCookiesList() {
        return getRequestHeaders("Cookie");
    }

    public List<RecordedHeader> getResponseSetCookiesList() {
        return getResponseHeaders("Set-Cookie");
    }

    public RecordedHeader getResponseHeader(String name) {
        return getHeader(name, responseHeaders);
    }

    public RecordedHeader getRequestHeader(String name) {
        return getHeader(name, requestHeaders);
    }

    // If case we want to look for more than one of the same named headers -  generally there shouldn't be more than one of each though
    public List<RecordedHeader> getResponseHeaders(String name) {
        return getHeaders(name, responseHeaders);
    }

    public List<RecordedHeader> getRequestHeaders(String name) {
        return getHeaders(name, requestHeaders);
    }

    private List<RecordedHeader> getHeaders(String name, List<RecordedHeader> headerList) {
        return headerList.stream().filter(header -> name.equals(header.getName())).collect(Collectors.toList());
    }

    private RecordedHeader getHeader(String name, List<RecordedHeader> headerList) {
        if (name == null || headerList == null) {
            return null;
        }

        for (RecordedHeader header : headerList) {
            if (name.equals(header.getName())) {
                return header;
            }
        }

        return null;
    }

    public String getIP() {
        if (!getHost().isEmpty()) {
            try {
                String host = getHost().substring(getHost().indexOf("//") + 2);
                InetAddress address = InetAddress.getByName(host);
                return address.getHostAddress();
            } catch (UnknownHostException e) {
                return "";
            }
        }
        return "";
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFullStatus() {
        return protocolVersion + " " + status + " " + statusText;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Boolean getHasParameters() {
        return url.contains("?") || "POST".equals(getMethod());
    }

    public String getBaseUrl() {
        if (url.contains("?")) {
            return url.substring(0, url.indexOf("?"));
        } else {
            return url;
        }
    }

    public String getHost() {
        return parseHost(url);
    }

    private String parseHost(String url) {
        if (url != null) {
            String host = url.substring(url.indexOf("//") + 2);
            if (host.contains("/")) {
                host = host.substring(0, host.indexOf("/"));
            }
            if (host.contains(":")) {
                host = host.substring(0, host.indexOf(":"));
            }
            return (isHttps ? "https://" : "http://") + host;
        }
        return "";
    }

    public Boolean getHttps() {
        return isHttps;
    }

    public void setHttps(Boolean https) {
        isHttps = https;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getRequest() {
        if ("".equals(request) || request == null) {
            RecordedRequestDAO recordedRequestDAO = new RecordedRequestDAO();
            request = recordedRequestDAO.getLazyRequest(this);
        }
        return request;
    }

    // Required to be an empty method
    // When loading the object we don't want to initially set this due to the memory foot print
    public void setRequest(InputStream inputStream) {

    }

    public void setRequest(String request) {
        if (request == null) {
            this.request = "";
        } else {
            this.request = request;
        }
    }

    public Integer getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(Integer requestNumber) {
        this.requestNumber = requestNumber;
    }

    public void addRecordedHeader(RecordedHeader recordedHeader) {
        if ("request".equals(recordedHeader.getType())) {
            requestHeaders.add(recordedHeader);
        } else if ("response".equals(recordedHeader.getType())) {
            responseHeaders.add(recordedHeader);
        }
    }

    public void addNewRequestHeader(String name, String value) {
        RecordedHeader requestHeader = RecordedHeader.create(RecordedHeader.class);
        requestHeader.setParent(this);
        requestHeader.setName(name);
        requestHeader.setValue(value);
        requestHeader.setType("request");
        requestHeader.save();
        requestHeaders.add(requestHeader);
    }

    public void addNewResponseHeader(String name, String value) {
        RecordedHeader responseHeader = RecordedHeader.create(RecordedHeader.class);
        responseHeader.setParent(this);
        responseHeader.setName(name);
        responseHeader.setValue(value);
        responseHeader.setType("response");
        responseHeader.save();
        responseHeaders.add(responseHeader);
    }

    // Required to be an empty method
    // When loading the object we don't want to initially set this due to the memory foot print
    public void setResponse(InputStream inputStream) {

    }

    public InputStream getRequestInputStream() {
        return new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
    }

    // This removes the request/response string to free memory and are lazy loaded when needed
    public void lighten() {
        response = "";
        request = "";
    }

    public String getResponse() {
        if ("".equals(response) || response == null) {
            RecordedRequestDAO recordedRequestDAO = new RecordedRequestDAO();
            response = recordedRequestDAO.getLazyResponse(this);
        }

        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public DateTime getResponseDateTimeFromHeaders() {
        RecordedHeader dateHeader = getResponseHeader("Date");
        if (dateHeader != null) {
            DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss zzz");
            return dateStringFormat.parseDateTime(dateHeader.getValue());
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

    public List<RecordedHeader> getRequestHeaders() {
        return requestHeaders;
    }

    public List<RecordedHeader> getResponseHeaders() {
        return responseHeaders;
    }

    public String getProxyConnectionString() {
        if (parentHttpProxy != null) {
            return parentHttpProxy.getConnectionString();
        }
        return "No Proxy";
    }
}
