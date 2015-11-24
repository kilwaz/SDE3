package application.net.proxy;

import application.data.model.DatabaseObject;

import java.util.UUID;

public class RecordedProxy extends DatabaseObject {
    private Integer requestCount = 0;

    public RecordedProxy(){
        super();
    }

    public Integer getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Integer requestCount) {
        this.requestCount = requestCount;
    }
}
