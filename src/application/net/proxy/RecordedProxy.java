package application.net.proxy;

import application.data.model.DatabaseObject;

public class RecordedProxy extends DatabaseObject {
    private Integer requestCount = 0;

    public RecordedProxy(Integer id) {
        super(id);
    }

    public Integer getRequestCount() {
        return requestCount;
    }
}
