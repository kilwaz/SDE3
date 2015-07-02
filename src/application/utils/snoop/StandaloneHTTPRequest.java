package application.utils.snoop;

import application.net.proxy.WebProxyRequest;
import application.net.proxy.WebProxyRequestManager;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
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

    private static Integer requestCount = 1;

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
        if (https) {
            executeHttps();
        } else {
            executeHttp();
        }
        return this;
    }

    public StandaloneHTTPRequest setRequestManager(WebProxyRequestManager webProxyRequestManager) {
        this.webProxyRequestManager = webProxyRequestManager;
        return this;
    }

    private StandaloneHTTPRequest executeHttps() {
        HttpsURLConnection connection = null;

        System.out.println("HTTPS TARGET IS " + destinationURL + " METHOD '" + method + "'");

        try {
            //Create connection
            URL url = new URL(destinationURL);

            System.out.println("HTTPS");
            connection = (HttpsURLConnection) url.openConnection();

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
                    System.out.println("REQUEST TO SERVER HEADER " + header + " = " + requestHeaders.get(header));
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

            //connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
            //connection.setRequestProperty("Content-Language", "en-US");

            //connection.setUseCaches(false);
            //connection.setDoOutput(true);

            // Write request body, only if we are doing post or put
            if ("POST".equals(method) || "PUT".equals(method)) {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.close();
            }

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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return this;
    }

    private void executeHttp() {
        HttpURLConnection connection = null;

        System.out.println("HTTP TARGET IS " + destinationURL + " METHOD '" + method + "'");

        try {
            WebProxyRequest webProxyRequest = new WebProxyRequest();
            webProxyRequest.setRequestUri(destinationURL);
            webProxyRequestManager.addNewActiveRequest(webProxyRequest.hashCode(), webProxyRequest);

            //Create connection
            URL url = new URL(destinationURL);
            connection = (HttpURLConnection) url.openConnection();

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
                    System.out.println("REQUEST TO SERVER HEADER " + header + " = " + requestHeaders.get(header));
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

            //Send request
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
            webProxyRequestManager.completeRequest(webProxyRequest.hashCode());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
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
