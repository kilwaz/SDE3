package application.net.proxy;

import application.node.implementations.RequestTrackerNode;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class WebProxyRequestManager {
    private ConcurrentHashMap<Integer, WebProxyRequest> activeRequests = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, WebProxyRequest> completedRequests = new ConcurrentHashMap<>();
    private List<RequestTrackerNode> linkedRequestTrackerNodes = new ArrayList<>();

    private static Logger log = Logger.getLogger(WebProxyRequestManager.class);

    private Integer requestCount = 1;

    public WebProxyRequestManager() {
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
            requestTrackerNode.addResult(webProxyRequest);
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

    public void completeRequest(Integer httpRequestHash) {
        WebProxyRequest webProxyRequest = getRequest(httpRequestHash);

        if (webProxyRequest != null) {
            completedRequests.put(httpRequestHash, webProxyRequest);
            activeRequests.remove(httpRequestHash);
            webProxyRequest.instantCompleteServerToProxy();
        }
    }

    public Callable<Boolean> haveAllRequestsFinished() {
        return () -> {
            return activeRequests.size() == 0; // The condition that must be fulfilled
        };
    }
}
