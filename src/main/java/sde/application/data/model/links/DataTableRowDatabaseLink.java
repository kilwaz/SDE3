package sde.application.data.model.links;

import sde.application.data.model.DatabaseLink;
import sde.application.node.implementations.DataTableNode;
import sde.application.node.objects.datatable.DataTableRow;

import java.util.UUID;

public class DataTableRowDatabaseLink extends DatabaseLink {
    public DataTableRowDatabaseLink() {
        super("data_table_rows", DataTableRow.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("node_id", method("getParentUuid"), method("setParent", DataTableNode.class)); // 2
    }
}
