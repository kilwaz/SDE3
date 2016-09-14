package application.net.proxy;

import org.apache.log4j.Logger;

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
    private Integer requestCount = 1;

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
            recordedRequest.setParentHttpProxy(recordedProxy);
            recordedRequest.setUrl(applyRedirects(webProxyRequest.getRequestURL()));
            recordedRequest.setDuration(webProxyRequest.getRequestDuration().intValue());
            recordedRequest.setRequestSize(webProxyRequest.getRequestContentSize());
            recordedRequest.setResponseSize(webProxyRequest.getResponseContentSize());
            recordedRequest.setRequest(webProxyRequest.getRequestContent());
            recordedRequest.setResponse(webProxyRequest.getResponseContent());
            recordedRequest.setRequestNumber(webProxyRequest.getRequestID());
            recordedRequest.setHttps(webProxyRequest.getHttps());
            recordedRequest.setMethod(webProxyRequest.getMethod());
            recordedRequest.setStatus(webProxyRequest.getStatus());
            recordedRequest.setRedirectUrl(webProxyRequest.getRedirectUrl());
            recordedRequest.setReference(recordedProxy.getProxyReference());

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
}
