package application.data.model.dao;

import application.data.SelectQuery;
import application.data.SelectResult;
import application.data.SelectResultRow;
import application.net.proxy.RecordedProxy;
import application.net.proxy.RecordedRequest;

import java.util.ArrayList;
import java.util.List;

public class RecordedRequestDAO {
    public RecordedRequestDAO() {

    }

    public List<RecordedRequest> getRecordedRequestByProxy(RecordedProxy recordedProxy) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from recorded_requests where id = ?")
                .addParameter(recordedProxy.getUuidString()) // 1
                .execute();
        List<RecordedRequest> recordedRequests = new ArrayList<>();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            recordedRequests.add(RecordedRequest.load(DAO.UUIDFromString(resultRow.getString("uuid")), RecordedRequest.class));
        }
        return recordedRequests;
    }
}
