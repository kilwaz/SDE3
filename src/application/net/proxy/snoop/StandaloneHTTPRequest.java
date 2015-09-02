package application.net.proxy.snoop;

import application.net.proxy.WebProxyRequest;
import application.net.proxy.WebProxyRequestManager;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class wraps an HTTP request from start to finish for use via the in built HTTP Proxy
 * <p>
 * To set the values you can use the setters as a chain like the example below, finally calling execute when all values have been set.
 * <p>
 * A {@link application.net.proxy.WebProxyRequestManager} must be set to manage the request.
 * <p>
 * <pre>
 * {@code
 *  StandaloneHTTPRequest standaloneHTTPRequest = new StandaloneHTTPRequest()
 *      .setUrl("http://www.example.com")
 *      .setMethod("GET")
 *      .setHttps(false)
 *      .setRequestManager(webProxyRequestManager)
 *      .execute();
 * }
 * </pre>
 * <p>
 * After the request has completed you can query the response.
 */

public class StandaloneHTTPRequest {
    private String method = "GET";
    private String destinationURL = "";
    private HashMap<String, String> responseHeaders = new HashMap<>();
    private HashMap<String, String> requestHeaders = new HashMap<>();
    private HashMap<String, String> requestParameters = new HashMap<>();
    private ByteBuffer response;
    private Boolean https = false;
    private Boolean hasCompleted = false;
    private WebProxyRequestManager webProxyRequestManager;

    private static Logger log = Logger.getLogger(StandaloneHTTPRequest.class);

    /**
     * Set the URL of the request.
     *
     * @param destinationURL The URL we want the request to go to.
     * @return Returns the instance of the {@link application.net.proxy.snoop.StandaloneHTTPRequest}
     */
    public StandaloneHTTPRequest setUrl(String destinationURL) {
        this.destinationURL = destinationURL;
        return this;
    }

    /**
     * Set the method of the request e.g. GET/POST
     *
     * @param method Method of the request
     * @return Returns the instance of the {@link application.net.proxy.snoop.StandaloneHTTPRequest}
     */
    public StandaloneHTTPRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * Sets the request headers of the request
     *
     * @param requestHeaders Headers of the request, in HashMap<String, String>
     * @return Returns the instance of the {@link application.net.proxy.snoop.StandaloneHTTPRequest}
     */
    public StandaloneHTTPRequest setRequestHeaders(HashMap<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
        return this;
    }

    /**
     * Sets the request parameters of the request
     *
     * @param requestParameters Parameters of the request, in HashMap<String, String>
     * @return Returns the instance of the {@link application.net.proxy.snoop.StandaloneHTTPRequest}
     */
    public StandaloneHTTPRequest setRequestParameters(HashMap<String, String> requestParameters) {
        this.requestParameters = requestParameters;
        return this;
    }

    /**
     * Sets whether this request should be treated as https
     *
     * @param https Boolean flag for https
     * @return Returns the instance of the {@link application.net.proxy.snoop.StandaloneHTTPRequest}
     */
    public StandaloneHTTPRequest setHttps(Boolean https) {
        this.https = https;
        return this;
    }

    /**
     * Executes the request.
     *
     * @return Returns the instance of the {@link application.net.proxy.snoop.StandaloneHTTPRequest}
     */
    public StandaloneHTTPRequest execute() {
        if (!hasCompleted) { // A request can only be run once
            executeHttp();
        }

        return this;
    }

    /**
     * Sets a {@link application.net.proxy.WebProxyRequestManager} to manage this request.
     *
     * @param webProxyRequestManager Manager for the request.
     * @return Returns the instance of the {@link application.net.proxy.snoop.StandaloneHTTPRequest}
     */
    public StandaloneHTTPRequest setRequestManager(WebProxyRequestManager webProxyRequestManager) {
        this.webProxyRequestManager = webProxyRequestManager;
        return this;
    }

    /**
     * Handles the construction of the request and the deconstruction of the response
     *
     * @return Returns the instance of the {@link application.net.proxy.snoop.StandaloneHTTPRequest}
     */
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

            InputStream is = null;
            int contentLength = 0;
            try {
                //Get Response
                is = connection.getInputStream();
                contentLength = connection.getContentLength();


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
            } catch (FileNotFoundException | MalformedURLException ex) { // 404
                String notFoundResponse = "HTTP/1.0 404 Not Found";
                response = ByteBuffer.wrap(notFoundResponse.getBytes());
            }

            webProxyRequest.instantCompleteServerToProxy();
            webProxyRequest.setResponseBuffer(response);
            webProxyRequest.setResponseHeaders(responseHeaders);
            webProxyRequestManager.completeRequest(webProxyRequest.hashCode());
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        hasCompleted = true;

        return this;
    }

    /**
     * Get the response headers
     *
     * @return HashMap of response headers, available only after the request has run
     */
    public HashMap<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Get the raw response
     *
     * @return Response as a byte buffer
     */
    public ByteBuffer getResponse() {
        return response;
    }

    /**
     * get the response as a string
     *
     * @return Response converted to readable string.
     */
    public String getResponseStr() {
        return new String(response.array());
    }

    /**
     * Returns if request has been completed with a response
     *
     * @return Boolean whether request has completed.
     */
    public Boolean getHasCompleted() {
        return hasCompleted;
    }
}
