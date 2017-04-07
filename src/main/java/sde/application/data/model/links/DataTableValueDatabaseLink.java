package sde.application.data.model.links;

import sde.application.data.model.DatabaseLink;
import sde.application.node.objects.datatable.DataTableRow;
import sde.application.node.objects.datatable.DataTableValue;

import java.util.UUID;

public class DataTableValueDatabaseLink extends DatabaseLink {
    public DataTableValueDatabaseLink() {
        super("data_table_values", DataTableValue.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("data_table_id", method("getParentUuid"), method("setParentRow", DataTableRow.class)); // 2
        link("data_key", method("getDataKey"), method("setDataKey", String.class)); // 3
        link("data_value", method("getDataValue"), method("setDataValue", String.class)); // 4
        link("data_order", method("getOrder"), method("setOrder", Integer.class)); // 5
    }
}
