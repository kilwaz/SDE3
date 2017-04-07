package sde.application.data.model.links;

import sde.application.data.model.DatabaseLink;
import sde.application.net.proxy.RecordedHeader;

import java.util.UUID;

public class RecordedHeaderDatabaseLink extends DatabaseLink {
    public RecordedHeaderDatabaseLink() {
        super("http_headers", RecordedHeader.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("request_id", method("getParentUuid"), null); // 2
        link("header_name", method("getName"), method("setName", String.class)); // 3
        link("header_value", method("getValue"), method("setValue", String.class)); // 4
        link("header_type", method("getType"), method("setType", String.class)); // 5
    }
}
