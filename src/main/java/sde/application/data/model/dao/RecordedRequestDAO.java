package sde.application.data.model.dao;

import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.data.UpdateQuery;
import sde.application.error.Error;
import sde.application.net.proxy.RecordedProxy;
import sde.application.net.proxy.RecordedRequest;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
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

    public InputStream getLazyRequestInputStream(RecordedRequest recordedRequest) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select request_content from recorded_requests where uuid = ?")
                .addParameter(recordedRequest.getUuidString()) // 1
                .execute();

        InputStream requestStream = new ByteArrayInputStream("".getBytes()); // Default blank input stream
        for (SelectResultRow resultRow : selectResult.getResults()) {
            requestStream = resultRow.getBlobInputStream("request_content");
        }

        return requestStream;
    }

    public String getLazyRequest(RecordedRequest recordedRequest) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select request_content from recorded_requests where uuid = ?")
                .addParameter(recordedRequest.getUuidString()) // 1
                .execute();
        String requestContent = "";
        for (SelectResultRow resultRow : selectResult.getResults()) {
            StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(resultRow.getBlobInputStream("request_content"), writer);
            } catch (IOException ex) {
                Error.BLOB_TO_STRING_CONVERT_FAILED.record().create(ex);
            }
            requestContent = writer.toString();
        }

        return requestContent;
    }

    public String getLazyResponse(RecordedRequest recordedRequest) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select response_content from recorded_requests where uuid = ?")
                .addParameter(recordedRequest.getUuidString()) // 1
                .execute();
        String responseContent = "";
        for (SelectResultRow resultRow : selectResult.getResults()) {

            StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(resultRow.getBlobInputStream("response_content"), writer);
            } catch (IOException ex) {
                Error.BLOB_TO_STRING_CONVERT_FAILED.record().create(ex);
            }
            responseContent = writer.toString();
        }

        return responseContent;
    }

    public InputStream getLazyResponseInputStream(RecordedRequest recordedRequest) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select response_content from recorded_requests where uuid = ?")
                .addParameter(recordedRequest.getUuidString()) // 1
                .execute();

        InputStream responseStream = new ByteArrayInputStream("".getBytes()); // Default blank input stream
        for (SelectResultRow resultRow : selectResult.getResults()) {
            responseStream = resultRow.getBlobInputStream("response_content");
        }

        return responseStream;
    }

    public void deleteAllRecordedRequests() {
        new UpdateQuery("update recorded_requests set forDelete = 1").execute();
    }
}
