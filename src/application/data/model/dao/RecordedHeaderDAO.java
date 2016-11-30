package application.data.model.dao;

import application.data.SelectQuery;
import application.data.SelectResult;
import application.data.SelectResultRow;
import application.data.UpdateQuery;
import application.net.proxy.RecordedHeader;
import application.net.proxy.RecordedRequest;

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
