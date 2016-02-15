package application.net.proxy;

import application.node.implementations.RequestTrackerNode;
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
    private List<RequestTrackerNode> linkedRequestTrackerNodes = new ArrayList<>();
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

    public void addRequestTrackerNode(RequestTrackerNode requestTrackerNode) {
        linkedRequestTrackerNodes.add(requestTrackerNode);
    }

    public Boolean isCurrentActiveRequest(Integer httpRequestHash) {
        return activeRequests.containsKey(httpRequestHash);
    }

    public void addNewActiveRequest(Integer httpRequestHash, WebProxyRequest webProxyRequest) {
        webProxyRequest.setRequestID(requestCount);
        requestCount++;

        activeRequests.put(httpRequestHash, webProxyRequest);

        // Add the result to linked request tracker nodes
        for (RequestTrackerNode requestTrackerNode : linkedRequestTrackerNodes) {
            //requestTrackerNode.addResult(recordedRequest);
        }
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

    public void completeRequest(Integer httpRequestHash) {
        WebProxyRequest webProxyRequest = getRequest(httpRequestHash);

        if (webProxyRequest != null) {
            //completedRequests.put(httpRequestHash, webProxyRequest);
            activeRequests.remove(httpRequestHash);
            webProxyRequest.instantCompleteServerToProxy();

            // Save the request to the database
            RecordedRequest recordedRequest = RecordedRequest.create(RecordedRequest.class);
            recordedRequest.setParentHttpProxy(recordedProxy);
            recordedRequest.setURL(applyRedirects(webProxyRequest.getRequestURL()));
            recordedRequest.setDuration(webProxyRequest.getRequestDuration().intValue());
            recordedRequest.setRequestSize(webProxyRequest.getRequestContentSize());
            recordedRequest.setResponseSize(webProxyRequest.getResponseContentSize());
            recordedRequest.setRequest(webProxyRequest.getRequestContent());
            recordedRequest.setResponse(webProxyRequest.getResponseContent());

            // Save the request headers
            for (String name : webProxyRequest.getRequestHeaders().keySet()) {
                recordedRequest.addNewRequestHeader(name, webProxyRequest.getRequestHeaders().get(name));
            }

            // Save the response headers
            for (String name : webProxyRequest.getResponseHeaders().keySet()) {
                recordedRequest.addNewResponseHeader(name, webProxyRequest.getResponseHeaders().get(name));
            }

            recordedRequest.save();

            for (RequestTrackerNode requestTrackerNode : linkedRequestTrackerNodes) {
                requestTrackerNode.addResult(recordedRequest);
            }
        }
    }

    public Callable<Boolean> haveAllRequestsFinished() {
        return () -> {
            return activeRequests.size() == 0; // The condition that must be fulfilled
        };
    }

    public RecordedProxy getRecordedProxy() {
        return recordedProxy;
    }
}
