package application.data.model.links;

import application.data.model.DatabaseLink;
import application.error.Error;
import application.net.proxy.RecordedHeader;

public class RecordedHeaderDatabaseLink extends DatabaseLink {
    public RecordedHeaderDatabaseLink() {
        super("http_headers");
        try {
            // Make sure the order is the same as column order in database
            link("request_id", RecordedHeader.class.getMethod("getParentRequestId")); // 1
            link("header_name", RecordedHeader.class.getMethod("getName")); // 2
            link("header_value", RecordedHeader.class.getMethod("getValue")); // 3
            link("header_type", RecordedHeader.class.getMethod("getType")); // 4
        } catch (NoSuchMethodException ex) {
            Error.DATA_LINK_METHOD_NOT_FOUND.record().create(ex);
        }
    }
}
