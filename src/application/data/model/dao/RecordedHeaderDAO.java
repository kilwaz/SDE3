package application.data.model.dao;

import application.data.*;
import application.net.proxy.RecordedHeader;
import application.net.proxy.RecordedRequest;

import java.util.ArrayList;
import java.util.List;

public class RecordedHeaderDAO {
    public RecordedHeaderDAO() {

    }

    public List<RecordedHeader> getRecordedHeadersByRequest(RecordedRequest recordedRequest) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from http_headers where request_id = ?")
                .addParameter(recordedRequest.getUuidString()) // 1
                .execute();
        List<RecordedHeader> recordedHeaders = new ArrayList<>();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            RecordedHeader recordedHeader = RecordedHeader.load(DAO.UUIDFromString(resultRow.getString("uuid")), RecordedHeader.class);
            recordedHeaders.add(recordedHeader);
        }
        return recordedHeaders;
    }

    public void deleteAllRecordedHeaders(){
        UpdateResult updateResult = (UpdateResult) new UpdateQuery("delete from http_headers").execute();
    }
}
