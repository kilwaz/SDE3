package application.net.proxy.snoop;

import application.net.proxy.WebProxyRequest;
import application.net.proxy.WebProxyRequestManager;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandaloneHTTPRequest {
    private String method = "GET";
    private String destinationURL = "";
    private HashMap<String, String> responseHeaders = new HashMap<>();
    private HashMap<String, String> requestHeaders = new HashMap<>();
    private HashMap<String, String> requestParameters = new HashMap<>();
    private ByteBuffer response;
    private Boolean https = false;
    private WebProxyRequestManager webProxyRequestManager;

    private static Logger log = Logger.getLogger(StandaloneHTTPRequest.class);

    public StandaloneHTTPRequest setUrl(String destinationURL) {
        this.destinationURL = destinationURL;
        return this;
    }

    public StandaloneHTTPRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    public StandaloneHTTPRequest setRequestHeaders(HashMap<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
        return this;
    }

    public StandaloneHTTPRequest setRequestParameters(HashMap<String, String> requestParameters) {
        this.requestParameters = requestParameters;
        return this;
    }

    public StandaloneHTTPRequest setHttps(Boolean https) {
        this.https = https;
        return this;
    }

    public StandaloneHTTPRequest execute() {
        executeHttp();
        return this;
    }

    public StandaloneHTTPRequest setRequestManager(WebProxyRequestManager webProxyRequestManager) {
        this.webProxyRequestManager = webProxyRequestManager;
        return this;
    }

    private StandaloneHTTPRequest executeHttp() {
        ProxyConnectionWrapper connection = null;
        try {
            WebProxyRequest webProxyRequest = new WebProxyRequest();
            webProxyRequest.setRequestUri(destinationURL);
            webProxyRequestManager.addNewActiveRequest(webProxyRequest.hashCode(), webProxyRequest);

            //Create connection
            URL url = new URL(destinationURL);

            connection = new ProxyConnectionWrapper(url, https);
            connection.setRequestMethod(method);
            if ("POST".equals(method) || "PUT".equals(method)) {
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);
            }

            // Sets the headers for the outgoing request
            for (String header : requestHeaders.keySet()) {
                if (!header.equals("If-None-Match")
                        && !header.equals("If-Modified-Since")
                        && !header.equals("Proxy-Connection")) {
                    connection.setRequestProperty(header, requestHeaders.get(header));
                }
            }

            // Sets the headers for the outgoing request
            String urlParameters = "";
            for (String parameter : requestParameters.keySet()) {
                if (urlParameters.equals("")) {
                    urlParameters = parameter + "=" + requestParameters.get(parameter);
                } else {
                    urlParameters += "&" + parameter + "=" + requestParameters.get(parameter);
                }
            }

            webProxyRequest.setRequestHeaders(requestHeaders);
            webProxyRequest.setRequestContent(urlParameters);

            // Write request body, only if we are doing post or put
            if ("POST".equals(method) || "PUT".equals(method)) {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.close();
            }

            // This starts the response timer
            webProxyRequest.instantStartProxyToServer();

            //Get Response
            InputStream is = connection.getInputStream();
            int contentLength = connection.getContentLength();

            Map<String, List<String>> connectionHeaders = connection.getHeaderFields();
            for (String header : connectionHeaders.keySet()) {
                String concatHeader = "";
                for (String headerValue : connectionHeaders.get(header)) {
                    concatHeader += " " + headerValue;
                }

                // We don't want these responseHeaders
                // null is the response code
                // As we are a proxy we don't want to keep their content length or transfer encoding as we will decide our own
                if (header != null && !header.equals("Transfer-Encoding") && !header.equals("Content-Length")) {
                    responseHeaders.put(header, concatHeader);
                }
            }

            ByteArrayOutputStream tmpOut;
            if (contentLength != -1) {
                tmpOut = new ByteArrayOutputStream(contentLength);
            } else {
                tmpOut = new ByteArrayOutputStream(16384); // Pick some appropriate size
            }

            byte[] buf = new byte[512];
            while (true) {
                int len = is.read(buf);
                if (len == -1) {
                    break;
                }
                tmpOut.write(buf, 0, len);
            }
            is.close();
            tmpOut.close(); // No effect, but good to do anyway to keep the metaphor alive

            byte[] array = tmpOut.toByteArray();

            response = ByteBuffer.wrap(array);

            webProxyRequest.instantCompleteServerToProxy();
            webProxyRequest.setResponseBuffer(response);
            webProxyRequest.setResponseHeaders(responseHeaders);
            webProxyRequestManager.completeRequest(webProxyRequest.hashCode());
        } catch (Exception ex) {
            log.error(ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return this;
    }

    public HashMap<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public ByteBuffer getResponse() {
        return response;
    }

    public String getResponseStr() {
        return new String(response.array());
    }
}
