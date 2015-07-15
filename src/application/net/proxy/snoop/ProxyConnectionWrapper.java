package application.net.proxy.snoop;


import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ProxyConnectionWrapper {
    private Boolean https;
    private URL destinationURL;

    private HttpURLConnection httpConnection;
    private HttpsURLConnection httpsConnection;

    public ProxyConnectionWrapper(URL destinationURL, Boolean https) throws IOException {
        this.destinationURL = destinationURL;
        this.https = https;

        if (https) {
            httpsConnection = (HttpsURLConnection) destinationURL.openConnection();
        } else {
            httpConnection = (HttpURLConnection) destinationURL.openConnection();
        }
    }

    public void setRequestMethod(String method) throws IOException {
        if (https) {
            httpsConnection.setRequestMethod(method);
        } else {
            httpConnection.setRequestMethod(method);
        }
    }

    public void setRequestProperty(String propertyName, String propertyValue) throws IOException {
        if (https) {
            httpsConnection.setRequestProperty(propertyName, propertyValue);
        } else {
            httpConnection.setRequestProperty(propertyName, propertyValue);
        }
    }

    public void setDoOutput(Boolean doOutput) throws IOException {
        if (https) {
            httpsConnection.setDoOutput(doOutput);
        } else {
            httpConnection.setDoOutput(doOutput);
        }
    }

    public InputStream getInputStream() throws IOException {
        if (https) {
            return httpsConnection.getInputStream();
        } else {
            return httpConnection.getInputStream();
        }
    }

    public OutputStream getOutputStream() throws IOException {
        if (https) {
            return httpsConnection.getOutputStream();
        } else {
            return httpConnection.getOutputStream();
        }
    }

    public int getContentLength() throws IOException {
        if (https) {
            return httpsConnection.getContentLength();
        } else {
            return httpConnection.getContentLength();
        }
    }

    public Map<String, List<String>> getHeaderFields() throws IOException {
        if (https) {
            return httpsConnection.getHeaderFields();
        } else {
            return httpConnection.getHeaderFields();
        }
    }

    public void disconnect() {
        if (https) {
            httpsConnection.disconnect();
        } else {
            httpConnection.disconnect();
        }
    }
}
