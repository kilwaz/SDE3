package sde.application.data.model.dao;

import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.data.UpdateQuery;
import sde.application.net.proxy.RecordedHeader;
import sde.application.net.proxy.RecordedRequest;

import java.util.ArrayList;
import java.util.List;

public class RecordedHeaderDAO {
    public RecordedHeaderDAO() {
    }

    public List<RecordedHeader> getRecordedRequestHeadersByRequest(RecordedRequest recordedRequest) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from http_headers where request_id = ? and header_type = ?")
                .addParameter(recordedRequest.getUuidString()) // 1
                .addParameter("request") // 2
                .execute();
        return processQueryResults(selectResult);
    }

    public List<RecordedHeader> getRecordedResponseHeadersByRequest(RecordedRequest recordedRequest) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from http_headers where request_id = ? and header_type = ?")
                .addParameter(recordedRequest.getUuidString()) // 1
                .addParameter("response") // 2
                .execute();
        return processQueryResults(selectResult);
    }

    public List<RecordedHeader> getRecordedHeadersByRequest(RecordedRequest recordedRequest) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from http_headers where request_id = ?")
                .addParameter(recordedRequest.getUuidString()) // 1
                .execute();
        return processQueryResults(selectResult);
    }

    private List<RecordedHeader> processQueryResults(SelectResult selectResult) {
        List<RecordedHeader> recordedHeaders = new ArrayList<>();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            RecordedHeader recordedHeader = RecordedHeader.load(DAO.UUIDFromString(resultRow.getString("uuid")), RecordedHeader.class);
            recordedHeaders.add(recordedHeader);
        }
        return recordedHeaders;
    }

    public void deleteAllRecordedHeaders() {
        new UpdateQuery("update http_headers set forDelete = 1").execute();
    }
}
