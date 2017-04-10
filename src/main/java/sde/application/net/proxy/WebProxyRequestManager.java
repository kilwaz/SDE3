package sde.application.net.proxy;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import sde.application.utils.AppParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class WebProxyRequestManager {
    private static Logger log = Logger.getLogger(WebProxyRequestManager.class);
    private ConcurrentHashMap<Integer, WebProxyRequest> activeRequests = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, WebProxyRequest> completedRequests = new ConcurrentHashMap<>();
    private List<ProxyRequestListener> linkedRequestListeners = new ArrayList<>();
    private HashMap<String, String> redirectURLs = new HashMap<>();
    private HashMap<String, BasicAuthUsernamePassword> basicAuthMapping = new HashMap<>();
    private Integer requestCount = 1;
    private Boolean isSSL = false;

    private HttpClient httpClient = null;
    private CookieStore httpCookieStore = null;

    private RecordedProxy recordedProxy;

    public WebProxyRequestManager() {
        recordedProxy = RecordedProxy.create(RecordedProxy.class);
        recordedProxy.save();
    }

    public void clearAllRedirectURLs() {
        redirectURLs.clear();
    }

    public void removeRedirectURL(String url) {
        redirectURLs.remove(url);
    }

    public void addBasicAuth(String url, BasicAuthUsernamePassword basicAuthUsernamePassword) {
        basicAuthMapping.put(url, basicAuthUsernamePassword);
    }

    public void addRedirectURL(String url, String redirect) {
        redirectURLs.put(url, redirect);
    }

    public void addProxyRequestListener(ProxyRequestListener requestTrackerNode) {
        if (!linkedRequestListeners.contains(requestTrackerNode)) { // Check to see if listener is already added, can't add a listener twice
            linkedRequestListeners.add(requestTrackerNode);
        }
    }

    public Boolean isCurrentActiveRequest(Integer httpRequestHash) {
        return activeRequests.containsKey(httpRequestHash);
    }

    public void addNewActiveRequest(Integer httpRequestHash, WebProxyRequest webProxyRequest) {
        numberCompletedRequest(webProxyRequest);
        activeRequests.put(httpRequestHash, webProxyRequest);
    }

    public void setRequestStatus(Integer httpRequestHash, Integer status) {
        activeRequests.get(httpRequestHash).setRequestStatus(status);
    }

    public WebProxyRequest getRequest(Integer httpRequestHash) {
        if (activeRequests.containsKey(httpRequestHash)) {
            return activeRequests.get(httpRequestHash);
        } else if (completedRequests.containsKey(httpRequestHash)) {
            return completedRequests.get(httpRequestHash);
        } else {
            return null;
        }
    }

    public BasicAuthUsernamePassword hasBasicAuth(String url) {
        for (String authUrl : basicAuthMapping.keySet()) {
            if (url.contains(authUrl)) {
                return basicAuthMapping.get(authUrl);
            }
        }

        return null;
    }

    public String applyRedirects(String url) {
        for (String from : redirectURLs.keySet()) {
            if (url.contains(from)) {
                url = url.replace(from, redirectURLs.get(from));
            }
        }

        return url;
    }

    // Assigns a request number once the request has been completed
    private synchronized void numberCompletedRequest(WebProxyRequest webProxyRequest) {
        webProxyRequest.setRequestID(requestCount);
        requestCount++;
    }

    public void completeRequest(Integer httpRequestHash) {
        WebProxyRequest webProxyRequest = getRequest(httpRequestHash);

        if (webProxyRequest != null) {
            activeRequests.remove(httpRequestHash);
            webProxyRequest.instantCompleteServerToProxy();

            // Save the request to the database
            RecordedRequest recordedRequest = RecordedRequest.create(RecordedRequest.class);
            recordedRequest.initNewObj();
            recordedRequest.setParentHttpProxy(recordedProxy);
            recordedRequest.setUrl(applyRedirects(webProxyRequest.getRequestURL()));
            recordedRequest.setDuration(webProxyRequest.getRequestDuration().intValue());
            recordedRequest.setRequestSize(webProxyRequest.getRequestContentSize());
            recordedRequest.setResponseSize(webProxyRequest.getResponseContentSize());
            recordedRequest.setRequest(webProxyRequest.getRequestContent());
            recordedRequest.setResponseBuffer(webProxyRequest.getResponseBuffer());
            recordedRequest.setRequestNumber(webProxyRequest.getRequestID());
            recordedRequest.setHttps(webProxyRequest.getHttps());
            recordedRequest.setMethod(webProxyRequest.getMethod());
            recordedRequest.setStatus(webProxyRequest.getStatus());
            recordedRequest.setRedirectUrl(webProxyRequest.getRedirectUrl());
            recordedRequest.setReference(recordedProxy.getProxyReference());
            recordedRequest.setProxyRequestReceivedTime(webProxyRequest.getInstantStartProxyToServer().toDateTime());
            recordedRequest.setDownloadTime(webProxyRequest.getDownloadTime());
            recordedRequest.setWaitTimeToFirstByte(webProxyRequest.getWaitTimeToFirstByte());
            recordedRequest.setStatusText(webProxyRequest.getStatusText());
            recordedRequest.setProtocolVersion(webProxyRequest.getProtocolVersion());
            recordedRequest.setCookies(webProxyRequest.getCookies());

            // Save the request headers
            for (String name : webProxyRequest.getRequestHeaders().keySet()) {
                recordedRequest.addNewRequestHeader(name, webProxyRequest.getRequestHeaders().get(name));
            }

            // Save the response headers
            for (String name : webProxyRequest.getResponseHeaders().keySet()) {
                recordedRequest.addNewResponseHeader(name, webProxyRequest.getResponseHeaders().get(name));
            }

            recordedRequest.save();
            //recordedRequest.lighten(); // Sets response to "" which will be reloaded from database if needed, to save memory

            // Apply the completed request to any linked tracker nodes
            for (ProxyRequestListener proxyRequestListener : linkedRequestListeners) {
                proxyRequestListener.addRequest(recordedRequest);
            }
        }
    }

    public Callable<Boolean> haveAllRequestsFinished() {
        return () -> {
            return activeRequests.size() == 0; // The condition that must be fulfilled
        };
    }

    public ConcurrentHashMap<Integer, WebProxyRequest> getActiveRequests() {
        return activeRequests;
    }

    public RecordedProxy getRecordedProxy() {
        return recordedProxy;
    }

    // Get or create the HttpClient
    public HttpClient getHttpClient() {
        if (httpClient == null) {
            RequestConfig requestConfig = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
            httpClient = HttpClientBuilder.create()
                    .setRetryHandler(new HttpClientRetryHandler(AppParams.getBrowserDefaultRetryCount())) // Get Default max retries
                    .setDefaultRequestConfig(requestConfig)
                    .setDefaultCookieStore(getHttpCookieStore())
                    .build();
        }
        return httpClient;
    }

    // Get or create the HttpCookieStore
    public CookieStore getHttpCookieStore() {
        if (httpCookieStore == null) {
            httpCookieStore = new BasicCookieStore();
        }
        return httpCookieStore;
    }

    public Boolean getSSL() {
        return isSSL;
    }

    public void setSSL(Boolean SSL) {
        isSSL = SSL;
    }
}
