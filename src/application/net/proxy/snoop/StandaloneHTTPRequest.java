package application.net.proxy.snoop;

import application.error.Error;
import application.net.proxy.BasicAuthUsernamePassword;
import application.net.proxy.WebProxyRequest;
import application.net.proxy.WebProxyRequestManager;
import application.utils.SDEUtils;
import application.utils.Timer;
import application.utils.managers.StatisticsManager;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.util.CharsetUtil;

import javax.net.ssl.SSLException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
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
    private static final String NOT_FOUND_RESPONSE = "HTTP/1.0 404 Not Found";
    private static final String SSL_EXCEPTION = "SSL could not be verified";
    private static final String INTERNAL_ERROR_RESPONSE = "HTTP/1.0 500 Internal Listener Error";
    private static Logger log = Logger.getLogger(StandaloneHTTPRequest.class);
    private static Integer maximumRetryCount = 2;
    private String method = "GET";
    private String destinationUrl = "";
    private String redirectUrl = "";
    private HashMap<String, String> responseHeaders = new HashMap<>();
    private HashMap<String, String> requestHeaders = new HashMap<>();
    private HashMap<String, String> requestParameters = new HashMap<>();
    private ByteBuffer response;
    private Boolean https = false;
    private Boolean hasCompleted = false;
    private WebProxyRequestManager webProxyRequestManager;
    private Integer currentRetryCount = 0;

    /**
     * Set the URL of the request.  Any redirects will be applied here
     *
     * @param redirectUrl The URL we want the request to go to.
     * @return Returns the instance of the {@link application.net.proxy.snoop.StandaloneHTTPRequest}
     */
    public StandaloneHTTPRequest setUrl(String redirectUrl) {
        if (webProxyRequestManager != null) {
            this.destinationUrl = webProxyRequestManager.applyRedirects(redirectUrl);
            this.redirectUrl = redirectUrl;
        } else {
            this.destinationUrl = redirectUrl;
        }
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
        StatisticsManager.getInstance().getTotalStatisticStore().incrementRequests();
        StatisticsManager.getInstance().getSessionStatisticStore().incrementRequests();

        ProxyConnectionWrapper connection = null;
        WebProxyRequest webProxyRequest = null;
        try {
            webProxyRequest = new WebProxyRequest();
            webProxyRequest.setRequestUri(destinationUrl);
            webProxyRequest.setRedirectUrl(redirectUrl);
            webProxyRequest.setHttps(https);
            webProxyRequest.setMethod(method);
            webProxyRequestManager.addNewActiveRequest(webProxyRequest.hashCode(), webProxyRequest);

            //Create connection
            int requestSize = 0;
            URL url = new URL(destinationUrl);
            connection = new ProxyConnectionWrapper(url, https, ProxyConnectionWrapper.HTTP_CLIENT_APACHE, webProxyRequestManager);
            connection.setRequestMethod(method);
            if ("POST".equals(method) || "PUT".equals(method) || "OPTIONS".equals(method)) {
                requestSize += connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);
            }

            // Check to see if we have Basic Auth info with this request
            BasicAuthUsernamePassword basicAuthUsernamePassword = webProxyRequestManager.hasBasicAuth(redirectUrl);
            if (basicAuthUsernamePassword != null) {
                String authString = basicAuthUsernamePassword.getUsername() + ":" + basicAuthUsernamePassword.getPassword();
                ChannelBuffer authChannelBuffer = ChannelBuffers.copiedBuffer(authString, CharsetUtil.UTF_8);
                ChannelBuffer encodedAuthChannelBuffer = Base64.encode(authChannelBuffer, false);
                requestHeaders.put(HttpHeaders.Names.AUTHORIZATION, "Basic " + encodedAuthChannelBuffer.toString(CharsetUtil.UTF_8));
            }

            // Sets the headers for the outgoing request
            for (String header : requestHeaders.keySet()) {
                if (!header.equals("If-None-Match")
                        && !header.equals("If-Modified-Since")
                        && !header.equals("Proxy-Connection")) {
                    requestSize += connection.setRequestProperty(header, requestHeaders.get(header));
                }
            }

            StatisticsManager.getInstance().getTotalStatisticStore().addRequestSize(requestSize);
            StatisticsManager.getInstance().getSessionStatisticStore().addRequestSize(requestSize);

            // Sets the parameters for the outgoing request
            // THIS IS REPLICATED IN PROXYCONNETIONWRAPPER.java
            String urlParameters = "";
            for (Map.Entry<String, String> entry : requestParameters.entrySet()) {
                if ("".equals(entry.getKey())) {
                    urlParameters = entry.getKey() + "=" + entry.getValue();
                } else {
                    urlParameters += "&" + entry.getKey() + "=" + entry.getValue();
                }
            }

            webProxyRequest.setRequestHeaders(requestHeaders);
            webProxyRequest.setRequestContent(urlParameters);

            connection.setRequestParameters(requestParameters);

            // This starts the response timer
            webProxyRequest.instantStartProxyToServer();

            InputStream is = null;
            Long downloadTime = -1L;
            try {
                // Get Response
                Timer downloadTimer = null;
                try {
                    is = connection.getInputStream();
                    downloadTimer = new Timer();
                } catch (ConnectException ex) { // Retry the connection if for some reason it timed out
                    if (currentRetryCount < maximumRetryCount) {
                        currentRetryCount++;
                        Error.HTTP_TIMEOUT.record().additionalInformation("URL " + url).additionalInformation("Retrying..." + currentRetryCount).create(ex);
                        executeHttp();
                    }
                } catch (UnknownHostException ex) {
                    Error.HTTP_UNKNOWN_HOST.record().additionalInformation("URL " + url).create(ex);
                }

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
                        responseHeaders.put(header, concatHeader.trim());
                    }
                }

                // If the response is encoded as a gzip, decompress it
                if (responseHeaders.containsKey("Content-Encoding") && "gzip".equals(responseHeaders.get("Content-Encoding"))) {
                    InputStream unzippedStream = SDEUtils.gUnzipInputStream(is);

                    // Convert the response stream into a buffer
                    response = SDEUtils.toByteBuffer(unzippedStream);
                    unzippedStream.close();
                } else {
                    // Convert the response stream into a buffer
                    response = SDEUtils.toByteBuffer(is);
                }

                // Close the response input stream
                if (is != null) {
                    is.close();
                }

                // Stop the download timer
                if (downloadTimer != null) {
                    downloadTime = downloadTimer.getTimeSince();
                }

                //StatisticsManager.getInstance().getTotalStatisticStore().addResponseSize(totalResponseLength);
                //StatisticsManager.getInstance().getSessionStatisticStore().addResponseSize(totalResponseLength);

                //tmpOut.close();

                //byte[] array = tmpOut.toByteArray();
                //response = ByteBuffer.wrap(array);
            } catch (FileNotFoundException | MalformedURLException ex) { // 404
                response = ByteBuffer.wrap(NOT_FOUND_RESPONSE.getBytes());
                Error.PROXY_REQUEST_NOT_FOUND.record().hideStackInLog().additionalInformation("URL: " + url).create(ex);
                StatisticsManager.getInstance().getTotalStatisticStore().incrementResponseCode(404);
                StatisticsManager.getInstance().getSessionStatisticStore().incrementResponseCode(404);
            } catch (SSLException ex) { // SSL Exception
                response = ByteBuffer.wrap(SSL_EXCEPTION.getBytes());
                Error.SSL_EXCEPTION.record().additionalInformation("URL: " + url).create(ex);
            } catch (IOException ex) { // 500
                response = ByteBuffer.wrap(INTERNAL_ERROR_RESPONSE.getBytes());
                //Error.PROXY_INTERNAL_SERVER_ERROR.record().hideStackInLog().additionalInformation("URL: " + url).create(ex);
                Error.PROXY_INTERNAL_SERVER_ERROR.record().additionalInformation("URL: " + url).create(ex);
                StatisticsManager.getInstance().getTotalStatisticStore().incrementResponseCode(500);
                StatisticsManager.getInstance().getSessionStatisticStore().incrementResponseCode(500);
            }

            webProxyRequest.instantCompleteServerToProxy();
            webProxyRequest.setResponseBuffer(response);
            webProxyRequest.setStatus(connection.getResponseStatus());
            webProxyRequest.setStatusText(connection.getResponseStatusText());
            webProxyRequest.setDownloadTime(downloadTime);
            webProxyRequest.setWaitTimeToFirstByte(connection.getWaitTimeToFirstByte());
            webProxyRequest.setProtocolVersion(connection.getProtocolVersion());
            webProxyRequest.setCookies(connection.getCookies());
            StatisticsManager.getInstance().getTotalStatisticStore().incrementResponseCode(connection.getResponseStatus());
            StatisticsManager.getInstance().getSessionStatisticStore().incrementResponseCode(connection.getResponseStatus());
            webProxyRequest.setResponseHeaders(responseHeaders);
        } catch (Exception ex) {
            Error.HTTP_PROXY_REQUEST.record().create(ex);
        } finally {
            if (webProxyRequest != null) { // Mark the request as finished
                webProxyRequestManager.completeRequest(webProxyRequest.hashCode());
            }
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
