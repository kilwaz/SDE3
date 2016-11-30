package application.net.proxy.snoop;


import application.error.Error;
import application.utils.Timer;
import application.utils.managers.StatisticsManager;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
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
    public final static int HTTP_CLIENT_APACHE = 1;
    public final static int HTTP_CLIENT_JAVA_API = 2;
    private static Logger log = Logger.getLogger(ProxyConnectionWrapper.class);
    private static HttpClient httpClient;
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
    private CookieStore httpCookieStore = new BasicCookieStore();

    public ProxyConnectionWrapper(URL destinationURL, Boolean https, int connectionMethod) throws IOException {
        this.https = https;
        this.connectionMethod = connectionMethod;
        this.destinationURL = destinationURL;

        if (connectionMethod == HTTP_CLIENT_APACHE) {
            if (httpClient == null) {  // Only build this once as it can be reused multiple times
                RequestConfig requestConfig = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
                httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setDefaultCookieStore(httpCookieStore).build();
            }
        } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
            if (https) {
                httpsConnection = (HttpsURLConnection) destinationURL.openConnection();
            } else {
                httpConnection = (HttpURLConnection) destinationURL.openConnection();
            }
        }
    }

    public void setRequestMethod(String method) throws IOException {
        this.method = method;

        if (connectionMethod == HTTP_CLIENT_APACHE) {
            // Nothing extra needs to be here
        } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
            if (https) {
                httpsConnection.setRequestMethod(method);
            } else {
                httpConnection.setRequestMethod(method);
            }
        }
    }

    public int setRequestProperty(String propertyName, String propertyValue) throws IOException {
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

        return propertyName.getBytes().length + propertyValue.getBytes().length + 2; // +2 for the ': ' between name and value
    }

    public void setDoOutput(Boolean doOutput) throws IOException {
        if (connectionMethod == HTTP_CLIENT_APACHE) {
            // Nothing needs to be done in this case
        } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
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
                URI destinationURI = new URI(destinationURL.getProtocol(), destinationURL.getHost(), destinationURL.getPath(), destinationURL.getQuery(), null);
                request.setURI(destinationURI);

                // Headers
                for (String headerName : requestHeaders.keySet()) {
                    if (!headerName.equals("Content-Length")) { // Content length is already supplied by http client
                        request.addHeader(headerName, requestHeaders.get(headerName));
                    }
                }

                // URL Params
                List<NameValuePair> urlParameters = new ArrayList<>();

                if (request instanceof HttpGet) {
                    for (String paramName : requestParameters.keySet()) {
                        urlParameters.add(new BasicNameValuePair(paramName, requestParameters.get(paramName)));
                    }
                } else if (request instanceof HttpPost) {
                    HttpPost postRequest = (HttpPost) request;

                    for (String paramName : requestParameters.keySet()) {
                        urlParameters.add(new BasicNameValuePair(paramName, requestParameters.get(paramName)));
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

    public void setRequestParameters(HashMap<String, String> requestParameters) {
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
                    for (String parameter : requestParameters.keySet()) {
                        if (urlParameters.equals("")) {
                            urlParameters = parameter + "=" + requestParameters.get(parameter);
                        } else {
                            urlParameters += "&" + parameter + "=" + requestParameters.get(parameter);
                        }
                    }

                    // Write request body, only if we are doing post or put
                    if ("POST".equals(method) || "PUT".equals(method)) {
                        DataOutputStream wr = new DataOutputStream(outputStream);
                        wr.writeBytes(urlParameters);
                        wr.close();

                        StatisticsManager.getInstance().getTotalStatisticStore().addRequestSize(urlParameters.getBytes().length);
                        StatisticsManager.getInstance().getSessionStatisticStore().addRequestSize(urlParameters.getBytes().length);
                    }
                }
            }
        } catch (IOException ex) {
            Error.PROXY_INTERNAL_SERVER_ERROR.record().create(ex);
        }
    }

    public int getContentLength() throws IOException {
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

    public Map<String, List<String>> getHeaderFields() throws IOException {
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

    public String getProtocolVersion() {
        if (connectionMethod == HTTP_CLIENT_APACHE) {
            if (httpResponse != null) {
                return httpResponse.getStatusLine().getProtocolVersion().toString();
            }
        } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
//            if (https) {
//                return httpsConnection.get();
//            } else {
//                return httpConnection.getResponseMessage();
//            }
        }
        return "";
    }

    public String getResponseStatusText() throws IOException {
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

    public List<Cookie> getCookies() {
        if (connectionMethod == HTTP_CLIENT_APACHE) {
            if (httpResponse != null && httpCookieStore != null) {
                return httpCookieStore.getCookies();
            }
        } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
//            if (https) {
//                return httpsConnection.getResponseMessage();
//            } else {
//                return httpConnection.getResponseMessage();
//            }
        }
        return new ArrayList<>();
    }

    public int getResponseStatus() throws IOException {
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

    public void disconnect() {
        if (connectionMethod == HTTP_CLIENT_APACHE) {
            // Nothing needs to be disconnected here
        } else if (connectionMethod == HTTP_CLIENT_JAVA_API) {
            if (https) {
                httpsConnection.disconnect();
            } else {
                httpConnection.disconnect();
            }
        }
    }

    public Long getWaitTimeToFirstByte() {
        return waitTimeToFirstByte;
    }
}
