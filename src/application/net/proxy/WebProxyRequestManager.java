package application.net.proxy;

import application.node.implementations.RequestTrackerNode;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WebProxyRequestManager {
    private HashMap<Integer, WebProxyRequest> activeRequests = new HashMap<>();
    private HashMap<Integer, WebProxyRequest> completedRequests = new HashMap<>();
    private List<RequestTrackerNode> linkedRequestTrackerNodes = new ArrayList<>();

    private Integer requestCount = 1;

    public WebProxyRequestManager() {
    }

    public void addRequestTrackerNode(RequestTrackerNode requestTrackerNode) {
        linkedRequestTrackerNodes.add(requestTrackerNode);
    }

    public Boolean isCurrentActiveRequest(Integer httpRequestHash) {
        return activeRequests.containsKey(httpRequestHash);
    }

    public void addFullHttpRepsonse(Integer httpRequestHash, FullHttpResponse fullHttpResponse) {
        activeRequests.get(httpRequestHash).addFullHttpResponse(fullHttpResponse);
    }

    public void addFullHttpRequest(Integer httpRequestHash, FullHttpRequest fullHttpRequest) {
        activeRequests.get(httpRequestHash).addFullHttpRequest(fullHttpRequest);
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

    public void addRequestHttpContentToRequest(Integer httpRequestHash, HttpObject httpObject) {
        activeRequests.get(httpRequestHash).addRequestHttpObject(httpObject);
    }

    public void addResponseHttpContentToRequest(Integer httpRequestHash, HttpObject httpObject) {
        activeRequests.get(httpRequestHash).addResponseHttpObject(httpObject);
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

        completedRequests.put(httpRequestHash, webProxyRequest);
        activeRequests.remove(httpRequestHash);
        webProxyRequest.instantCompleteServerToProxy();
    }
}
