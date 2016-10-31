package application.data.export.har;

import application.net.proxy.RecordedHeader;
import application.net.proxy.RecordedRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HarExportHelper {
    private List<RecordedRequest> requestList = new ArrayList<>();
    private Log harLog = new Log();

    private HarExportHelper() {
    }

    public static HarExportHelper build() {
        return new HarExportHelper();
    }

    public HarExportHelper withRequests(List<RecordedRequest> requestList) {
        this.requestList = requestList;
        return this;
    }

    public void export() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        for (RecordedRequest recordedRequest : requestList) {
            Page page = new Page();

            Entry entry = new Entry();
            entry.setStartedDateTime(formatter.format(recordedRequest.getProxyRequestReceivedTime().toDate()));
            entry.setServerIPAddress(recordedRequest.getIP());
            entry.setTime(recordedRequest.getDuration());
            entry.setConnection(recordedRequest.getProxyConnectionString());

            // Request
            entry.getRequest().setMethod(recordedRequest.getMethod());
            entry.getRequest().setUrl(recordedRequest.getUrl());
            entry.getRequest().getPostData().setMimeType("None");
            entry.getRequest().setBodySize(recordedRequest.getRequestSize());

            // Request Headers
            for (RecordedHeader recordedHeader : recordedRequest.getRequestHeaders()) {
                Header header = new Header();
                header.setName(recordedHeader.getName());
                header.setValue(recordedHeader.getValue());
                entry.getRequest().getHeaders().addHeader(header);
            }

            // Response Status
            entry.getResponse().setStatus(recordedRequest.getStatus());
            entry.getResponse().setStatusText(recordedRequest.getStatusText());
            entry.getResponse().setHttpVersion(recordedRequest.getProtocolVersion());

            // Response Content
            entry.getResponse().getContent().setText(recordedRequest.getResponse());
            entry.getResponse().getContent().setSize(recordedRequest.getResponseSize());
            entry.getResponse().getContent().setMimeType(recordedRequest.getMediaType());

            // Response cookies
            for (org.apache.http.cookie.Cookie cookie : recordedRequest.getCookies()) {
                Cookie cookieEntry = new Cookie();
                cookieEntry.setName(cookie.getName());
                cookieEntry.setValue(cookie.getValue());
                cookieEntry.setDomain(cookie.getDomain());
                if (cookie.getExpiryDate() != null) {
                    cookieEntry.setExpires(formatter.format(cookie.getExpiryDate()));
                }
                //cookieEntry.setHttpOnly(Boolean.toString(cookie.)); // We can't get this yet..
                cookieEntry.setSecure(Boolean.toString(cookie.isSecure()));

                entry.getResponse().getCookies().addCookie(cookieEntry);
            }

            // Response Headers
            for (RecordedHeader recordedHeader : recordedRequest.getResponseHeaders()) {
                Header header = new Header();
                header.setName(recordedHeader.getName());
                header.setValue(recordedHeader.getValue());
                entry.getResponse().getHeaders().addHeader(header);
            }

            // Response Timings
            entry.getTimings().setWait(recordedRequest.getWaitTimeToFirstByte().intValue());
            entry.getTimings().setReceive(recordedRequest.getDownloadTime().intValue());

            harLog.getEntries().addEntry(entry);
        }

        // Do the final export
        harLog.export();
    }
}
