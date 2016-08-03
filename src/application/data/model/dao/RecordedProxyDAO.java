package application.data.model.dao;

import application.data.UpdateQuery;


public class RecordedProxyDAO {
    public RecordedProxyDAO() {

    }

    public void deleteAllRecordedProxies() {
        new UpdateQuery("delete from http_proxies").execute();
    }
}
