package application.net.proxy;

import application.data.model.DatabaseObject;

public class RecordedProxy extends DatabaseObject {
    private Integer requestCount = 0;
    private String connectionString = "Unknown";
    private String proxyReference = "";

    public RecordedProxy() {
        super();
    }

    public Integer getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Integer requestCount) {
        this.requestCount = requestCount;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getProxyReference() {
        return proxyReference;
    }

    public void setProxyReference(String proxyReference) {
        this.proxyReference = proxyReference;
    }
}
