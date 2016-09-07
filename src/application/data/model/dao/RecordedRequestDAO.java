package application.data.model.dao;

import application.data.SelectQuery;
import application.data.SelectResult;
import application.data.SelectResultRow;
import application.data.UpdateQuery;
import application.net.proxy.RecordedProxy;
import application.net.proxy.RecordedRequest;

import java.util.ArrayList;
import java.util.List;

public class RecordedRequestDAO {
    public RecordedRequestDAO() {

    }

    public List<RecordedRequest> getRecordedRequestByProxy(RecordedProxy recordedProxy) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from recorded_requests where http_proxy_id = ?")
                .addParameter(recordedProxy.getUuidString()) // 1
                .execute();
        List<RecordedRequest> recordedRequests = new ArrayList<>();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            recordedRequests.add(RecordedRequest.load(DAO.UUIDFromString(resultRow.getString("uuid")), RecordedRequest.class));
        }
        return recordedRequests;
    }

    public String getLazyRequest(RecordedRequest recordedRequest) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select request_content from recorded_requests where uuid = ?")
                .addParameter(recordedRequest.getUuidString()) // 1
                .execute();
        String requestContent = "";
        for (SelectResultRow resultRow : selectResult.getResults()) {
            requestContent = resultRow.getString("request_content");
        }

        return requestContent;
    }

    public String getLazyResponse(RecordedRequest recordedRequest) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select response_content from recorded_requests where uuid = ?")
                .addParameter(recordedRequest.getUuidString()) // 1
                .execute();
        String responseContent = "";
        for (SelectResultRow resultRow : selectResult.getResults()) {
            responseContent = resultRow.getString("response_content");
        }

        return responseContent;
    }

    public void deleteAllRecordedRequests() {
        new UpdateQuery("update recorded_requests set forDelete = 1").execute();
    }
}
