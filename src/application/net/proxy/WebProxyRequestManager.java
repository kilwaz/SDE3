package application.net.proxy;

import application.node.implementations.RequestTrackerNode;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WebProxyRequestManager {
    private HashMap<HttpRequest, WebProxyRequest> activeRequests = new HashMap<>();
    private HashMap<HttpRequest, WebProxyRequest> completedRequests = new HashMap<>();
    private List<RequestTrackerNode> linkedRequestTrackerNodes = new ArrayList<>();

    private Integer requestCount = 1;

    public WebProxyRequestManager() {

    }

    public void addRequestTrackerNode(RequestTrackerNode requestTrackerNode) {
        linkedRequestTrackerNodes.add(requestTrackerNode);
    }

    public Boolean isCurrentActiveRequest(HttpRequest httpRequest) {
        return activeRequests.containsKey(httpRequest);
    }

    public void addNewActiveRequest(HttpRequest httpRequest) {
        WebProxyRequest webProxyRequest = new WebProxyRequest(httpRequest, requestCount);
        requestCount++;

        webProxyRequest.instantStartProxyToServer();
        activeRequests.put(httpRequest, webProxyRequest);

        // Add the result to linked request tracker nodes
        for (RequestTrackerNode requestTrackerNode : linkedRequestTrackerNodes) {
            requestTrackerNode.addResult(webProxyRequest);
        }
    }

    public void addRequestHttpContentToRequest(HttpRequest httpRequest, HttpObject httpObject) {
        activeRequests.get(httpRequest).addRequestHttpObject(httpObject);
    }

    public void addResponseHttpContentToRequest(HttpRequest httpRequest, HttpObject httpObject) {
        activeRequests.get(httpRequest).addResponseHttpObject(httpObject);
    }

    public void setRequestStatus(HttpRequest httpRequest, Integer status) {
        activeRequests.get(httpRequest).setRequestStatus(status);
    }

    public WebProxyRequest getRequest(HttpRequest httpRequest) {
        if (activeRequests.containsKey(httpRequest)) {
            return activeRequests.get(httpRequest);
        } else if (completedRequests.containsKey(httpRequest)) {
            return completedRequests.get(httpRequest);
        } else {
            return null;
        }
    }

    public void completeRequest(HttpRequest httpRequest) {
        WebProxyRequest webProxyRequest = getRequest(httpRequest);

        completedRequests.put(httpRequest, webProxyRequest);
        activeRequests.remove(httpRequest);
        webProxyRequest.instantCompleteServerToProxy();

        //System.out.println(httpRequest.hashCode() + "***** Finished request from Browser - " + httpRequest.getUri() + "(" + webProxyRequest.getRequestHttpObjectCount() + ")(" + webProxyRequest.getResponseHttpObjectCount() + ") - " + webProxyRequest.getRequestDuration() + "ms");
    }
}
