package application.net.proxy;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.UUID;

public class MetaRecordedRequest {
    private static Logger log = Logger.getLogger(MetaRecordedRequest.class);

    private UUID recordedRequestUUID;
    private String url, host, method, reference, extension, title, ip, mediaType, cookies, redirectHost, proxyConnectionString;
    private Integer requestNumber, status, duration, responseSize, requestSize;
    private Boolean isHttps, hasParameters;
    private DateTime requestTime;

    public MetaRecordedRequest(RecordedRequest recordedRequest) {
        this.url = recordedRequest.getUrl().intern();
        this.host = recordedRequest.getHost().intern();
        this.requestNumber = recordedRequest.getRequestNumber();
        this.method = recordedRequest.getMethod().intern();
        this.status = recordedRequest.getStatus();
        this.recordedRequestUUID = recordedRequest.getUuid();
        this.duration = recordedRequest.getDuration();
        this.responseSize = recordedRequest.getResponseSize();
        this.reference = recordedRequest.getReference().intern();
        this.extension = recordedRequest.getExtension().intern();
        this.title = recordedRequest.getTitle().intern();
        this.isHttps = recordedRequest.getHttps();
        this.ip = recordedRequest.getIP().intern();
        this.mediaType = recordedRequest.getMediaType().intern();
        this.hasParameters = recordedRequest.getHasParameters();
        this.cookies = recordedRequest.getCookiesAsString().intern();
        this.requestTime = recordedRequest.getResponseDateTimeFromHeaders();
        this.requestSize = recordedRequest.getRequestSize();
        this.redirectHost = recordedRequest.getRedirectHost().intern();
        this.proxyConnectionString = recordedRequest.getProxyConnectionString().intern();
    }

    // Use with caution
    public RecordedRequest getRecordedRequest() {
        return RecordedRequest.load(recordedRequestUUID, RecordedRequest.class);
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }

    public Integer getRequestNumber() {
        return requestNumber;
    }

    public UUID getRecordedRequestUUID() {
        return recordedRequestUUID;
    }

    public String getMethod() {
        return method;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getDuration() {
        return duration;
    }

    public Integer getResponseSize() {
        return responseSize;
    }

    public String getReference() {
        return reference;
    }

    public String getExtension() {
        return extension;
    }

    public String getTitle() {
        return title;
    }

    public Boolean getHttps() {
        return isHttps;
    }

    public String getIP() {
        return ip;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getCookies() {
        return cookies;
    }

    public Boolean getHasParameters() {
        return hasParameters;
    }

    public DateTime getRequestTime() {
        return requestTime;
    }

    public Integer getRequestSize() {
        return requestSize;
    }

    public String getRedirectHost() {
        return redirectHost;
    }

    public String getProxyConnectionString() {
        return proxyConnectionString;
    }

    public String getBaseUrl() {
        if (url.contains("?")) {
            return url.substring(0, url.indexOf("?"));
        } else {
            return url;
        }
    }
}
