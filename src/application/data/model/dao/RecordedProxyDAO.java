package application.data.model.dao;

import application.data.UpdateQuery;
import application.data.UpdateResult;


public class RecordedProxyDAO {
    public RecordedProxyDAO() {

    }

    public void deleteAllRecordedProxies() {
        UpdateResult updateResult = (UpdateResult) new UpdateQuery("delete from http_proxies").execute();
    }
}
