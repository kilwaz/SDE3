package sde.application.data.model.dao;

import sde.application.data.UpdateQuery;


public class RecordedProxyDAO {
    public RecordedProxyDAO() {

    }

    public void deleteAllRecordedProxies() {
        new UpdateQuery("update http_proxies set forDelete = 1").execute();
    }
}
