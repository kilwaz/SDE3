package sde.application.net.proxy.snoop;


import sde.application.error.Error;
import sde.application.net.proxy.WebProxyRequestManager;
import sde.application.utils.Timer;
import sde.application.utils.managers.StatisticsManager;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyConnectionWrapper {
    final static int HTTP_CLIENT_APACHE = 1;
    final static int HTTP_CLIENT_JAVA_API = 2;
    private static Logger log = Logger.getLogger(ProxyConnectionWrapper.class);
    private HttpClient httpClient = null;
    private Boolean https;
    private HttpURLConnection httpConnection;
    private HttpsURLConnection httpsConnection;
    private String method;
    private int connectionMethod;
    private HashMap<String, String> requestHeaders = new HashMap<>();
    private HashMap<String, String> requestParameters = new HashMap<>();
    private URL destinationURL;
    private HttpResponse httpResponse;
    private Long waitTimeToFirstByte = -1L;
    private CookieStore httpCookieStore = null;

    ProxyConnectionWrapper(URL destinationURL, Boolean https, int connectionMethod, WebProxyRequestManager webProxyRequestManager) throws IOException {
        this.https = https;
        this.connectionMethod = connectionMethod;
        this.destinationURL = destinationURL;

        if (connectionMethod == HTTP_CLIENT_APACHE) {
            httpClient = webProxyRequestManager.getHttpClient();
            httpCookieStore = webProxyRequestManager.getHttpCookieStore();
        } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
            if (https) {
                httpsConnection = (HttpsURLConnection) destinationURL.openConnection();
            } else {
                httpConnection = (HttpURLConnection) destinationURL.openConnection();
            }
        }
    }

    void setRequestMethod(String method) throws IOException {
        this.method = method;

        // We don't need to do anything for HTTP_CLIENT_APACHE mode, only HTTP_CLIENT_JAVA_API
        if (connectionMethod == HTTP_CLIENT_JAVA_API) {
            if (https) {
                httpsConnection.setRequestMethod(method);
            } else {
                httpConnection.setRequestMethod(method);
            }
        }
    }

    int setRequestProperty(String propertyName, String propertyValue) throws IOException {
        if (propertyName == null || propertyValue == null) {
            return 0;
        }
        if (connectionMethod == HTTP_CLIENT_APACHE) {
            requestHeaders.put(propertyName, propertyValue);
        } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
            if (https) {
                httpsConnection.setRequestProperty(propertyName, propertyValue);
            } else {
                httpConnection.setRequestProperty(propertyName, propertyValue);
            }
        }

        return propertyName.getBytes("UTF-8").length + propertyValue.getBytes("UTF-8").length + 2; // +2 for the ': ' between name and value
    }

    // This only applies to HTTP_CLIENT_JAVA_API, nothing needs to be done for HTTP_CLIENT_APACHE
    void setDoOutput(Boolean doOutput) throws IOException {
        if (connectionMethod == HTTP_CLIENT_JAVA_API) {
            if (https) {
                httpsConnection.setDoOutput(doOutput);
            } else {
                httpConnection.setDoOutput(doOutput);
            }
        }
    }

    public InputStream getInputStream() throws IOException {
        try {
            if (connectionMethod == HTTP_CLIENT_APACHE) {
                HttpRequestBase request;
                switch (method) {
                    case "GET":
                        request = new HttpGet();
                        break;
                    case "POST":
                        request = new HttpPost();
                        break;
                    default:
                        return null;
                }

                // URL
                URI destinationURI = new URI(destinationURL.getProtocol(), null, destinationURL.getHost(), destinationURL.getPort(), destinationURL.getPath(), destinationURL.getQuery(), null);
                //URI destinationURI = new URI(destinationURL.getProtocol(), destinationURL.getHost(), destinationURL.getPath(), destinationURL.getQuery(), null);
                request.setURI(destinationURI);

                // Headers
                for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                    if (!entry.getKey().equals("Content-Length")) { // Content length is already supplied by http client
                        request.addHeader(entry.getKey(), entry.getValue());
                    }
                }

                // URL Params
                List<NameValuePair> urlParameters = new ArrayList<>();

                if (request instanceof HttpGet) {
                    for (Map.Entry<String, String> entry : requestParameters.entrySet()) {
                        urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                    }
                } else if (request instanceof HttpPost) {
                    HttpPost postRequest = (HttpPost) request;

                    for (Map.Entry<String, String> entry : requestParameters.entrySet()) {
                        urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                    }
                    postRequest.setEntity(new UrlEncodedFormEntity(urlParameters));
                }
                Timer responseTime = new Timer();
                httpResponse = httpClient.execute(request);
                waitTimeToFirstByte = responseTime.getTimeSince();
                if (httpResponse != null && httpResponse.getEntity() != null) {
                    return httpResponse.getEntity().getContent();
                } else {
                    return null;
                }
            } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
                if (https) {
                    return httpsConnection.getInputStream();
                } else {
                    return httpConnection.getInputStream();
                }
            }
        } catch (URISyntaxException ex) {
            Error.HTTP_PROXY_REQUEST_FAILED.record().create(ex);
        }
        return null;
    }

    void setRequestParameters(HashMap<String, String> requestParameters) {
        try {
            if (connectionMethod == HTTP_CLIENT_APACHE) {
                this.requestParameters = requestParameters;
            } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
                OutputStream outputStream;
                if (https) {
                    outputStream = httpsConnection.getOutputStream();
                } else {
                    outputStream = httpConnection.getOutputStream();
                }

                if (outputStream != null) {
                    String urlParameters = "";
                    for (Map.Entry<String, String> entry : requestParameters.entrySet()) {
                        if (urlParameters.equals("")) {
                            urlParameters = entry.getKey() + "=" + entry.getValue();
                        } else {
                            urlParameters += "&" + entry.getKey() + "=" + entry.getValue();
                        }
                    }

                    // Write request body, only if we are doing post or put
                    if ("POST".equals(method) || "PUT".equals(method)) {
                        DataOutputStream wr = new DataOutputStream(outputStream);
                        wr.writeBytes(urlParameters);
                        wr.close();

                        StatisticsManager.getInstance().getTotalStatisticStore().addRequestSize(urlParameters.getBytes("UTF-8").length);
                        StatisticsManager.getInstance().getSessionStatisticStore().addRequestSize(urlParameters.getBytes("UTF-8").length);
                    }
                }
            }
        } catch (IOException ex) {
            Error.PROXY_INTERNAL_SERVER_ERROR.record().create(ex);
        }
    }

    int getContentLength() throws IOException {
        if (connectionMethod == HTTP_CLIENT_APACHE && httpResponse != null) {
            Header[] headers = httpResponse.getHeaders("Content-Length");
            if (headers.length > 0 && headers[0] != null) {
                try {
                    return Integer.parseInt(headers[0].getValue());
                } catch (NumberFormatException ex) {
                    Error.HTTP_PROXY_UNABLE_TO_PARSE_CONTENT_LENGTH.record().create(ex);
                    return -1;
                }
            }
        } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
            if (https) {
                return httpsConnection.getContentLength();
            } else {
                return httpConnection.getContentLength();
            }
        }
        return -1;
    }

    Map<String, List<String>> getHeaderFields() throws IOException {
        if (connectionMethod == HTTP_CLIENT_APACHE) {
            Map<String, List<String>> headers = new HashMap<>();

            if (httpResponse != null) {
                for (Header header : httpResponse.getAllHeaders()) {
                    List<String> elements = new ArrayList<>();
                    elements.add(header.getValue());
                    headers.put(header.getName(), elements);
                }
            }
            return headers;
        } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
            if (https) {
                return httpsConnection.getHeaderFields();
            } else {
                return httpConnection.getHeaderFields();
            }
        }
        return new HashMap<>();
    }

    // Only applies to HTTP_CLIENT_APACHE
    String getProtocolVersion() {
        if (connectionMethod == HTTP_CLIENT_APACHE) {
            if (httpResponse != null) {
                return httpResponse.getStatusLine().getProtocolVersion().toString();
            }
        }
        return "";
    }

    String getResponseStatusText() throws IOException {
        if (connectionMethod == HTTP_CLIENT_APACHE) {
            if (httpResponse != null) {
                return httpResponse.getStatusLine().getReasonPhrase();
            }
        } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
            if (https) {
                return httpsConnection.getResponseMessage();
            } else {
                return httpConnection.getResponseMessage();
            }
        }
        return "";
    }

    // Only applies to HTTP_CLIENT_APACHE
    public List<Cookie> getCookies() {
        if (connectionMethod == HTTP_CLIENT_APACHE) {
            if (httpResponse != null && httpCookieStore != null) {
                return httpCookieStore.getCookies();
            }
        }

        return new ArrayList<>();
    }

    int getResponseStatus() throws IOException {
        if (connectionMethod == HTTP_CLIENT_APACHE) {
            if (httpResponse != null) {
                return httpResponse.getStatusLine().getStatusCode();
            }
        } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
            if (https) {
                return httpsConnection.getResponseCode();
            } else {
                return httpConnection.getResponseCode();
            }
        }
        return -1;
    }

    // Only applies to HTTP_CLIENT_JAVA_API
    void disconnect() {
        if (connectionMethod == HTTP_CLIENT_JAVA_API) {
            if (https) {
                httpsConnection.disconnect();
            } else {
                httpConnection.disconnect();
            }
        }
    }

    Long getWaitTimeToFirstByte() {
        return waitTimeToFirstByte;
    }
}
