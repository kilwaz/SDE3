package sde.application.data.model.dao;

import org.apache.log4j.Logger;
import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.data.model.links.RecordedProxyDatabaseLink;
import sde.application.net.proxy.RecordedProxy;

import java.util.ArrayList;
import java.util.List;


public class RecordedProxyDAO {
    private static Logger log = Logger.getLogger(RecordedProxyDAO.class);

    public RecordedProxyDAO() {

    }

    public List<RecordedProxy> getAllProxies() {
        return getAllProxies(-1);
    }

    public List<RecordedProxy> getAllProxies(Integer limitedTo) {
        RecordedProxyDatabaseLink recordedProxyDatabaseLink = new RecordedProxyDatabaseLink();

        String limitedToStr = "";
        if (limitedTo > -1) {
            limitedToStr = " limit " + limitedTo;
        }

        SelectQuery selectQuery = new SelectQuery("select uuid from " + recordedProxyDatabaseLink.getTableName() + limitedToStr);
        SelectResult selectResult = (SelectResult) selectQuery.execute();

        List<RecordedProxy> recordedProxies = new ArrayList<>();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            String uuid = resultRow.getString("uuid");
            log.info("Loading " + uuid);
            recordedProxies.add(RecordedProxy.load(DAO.UUIDFromString(uuid), RecordedProxy.class));
        }

        return recordedProxies;
    }
}
