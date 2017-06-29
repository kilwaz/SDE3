package sde.application.test;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import sde.application.net.proxy.HttpClientRetryHandler;
import sde.application.utils.AppParams;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RemoteTestNode {
    private static Logger log = Logger.getLogger(RemoteTestNode.class);
    private RemoteTestNodeURL remoteTestNodeURL;
    private RemoteWebDriver remoteWebDriver;
    private String nodeId;
    private String nodeHost;
    private String referenceId = "NoID";

    public RemoteTestNode() {

    }

    public RemoteTestNode setRemoteTestNodeURL(RemoteTestNodeURL remoteTestNodeURL) {
        this.remoteTestNodeURL = remoteTestNodeURL;
        return this;
    }

    public RemoteTestNode setWebDriver(WebDriver webDriver) {
        this.remoteWebDriver = (RemoteWebDriver) webDriver;

        findNodeId();
        findNodeHost();

        referenceId = "RECORDING-" + remoteWebDriver.getSessionId();

        return this;
    }

    private void findNodeId() {
        HttpClient httpClient = HttpClientBuilder.create()
                .setRetryHandler(new HttpClientRetryHandler(AppParams.getBrowserDefaultRetryCount())) // Get Default max retries
                .build();

        HttpRequestBase request = new HttpGet();

        try {
            URI destinationURI = new URI("http", null, remoteTestNodeURL.getHubIp(), remoteTestNodeURL.getHubPort(), "/grid/api/testsession", "session=" + remoteWebDriver.getSessionId(), null);
            request.setURI(destinationURI);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            HttpResponse httpResponse = httpClient.execute(request);
            String json = IOUtils.toString(httpResponse.getEntity().getContent());
            JSONObject jsonObject = new JSONObject(json);

            nodeId = jsonObject.getString("proxyId");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findNodeHost() {
        HttpClient httpClient = HttpClientBuilder.create()
                .setRetryHandler(new HttpClientRetryHandler(AppParams.getBrowserDefaultRetryCount())) // Get Default max retries
                .build();

        HttpRequestBase request = new HttpGet();

        try {
            URI destinationURI = new URI("http", null, remoteTestNodeURL.getHubIp(), remoteTestNodeURL.getHubPort(), "/grid/api/proxy", "id=" + this.nodeId, null);
            request.setURI(destinationURI);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            HttpResponse httpResponse = httpClient.execute(request);
            String json = IOUtils.toString(httpResponse.getEntity().getContent());
            JSONObject jsonObject = new JSONObject(json);

            nodeHost = jsonObject.getJSONObject("request").getJSONObject("configuration").getString("host");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public RemoteTestNodeURL getRemoteTestNodeURL() {
        return remoteTestNodeURL;
    }

    public WebDriver getWebDriver() {
        return remoteWebDriver;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getScreenNumber() {
        if (nodeId != null) {
            return nodeId.substring(nodeId.lastIndexOf("-") + 1);
        }
        return "";
    }
}
