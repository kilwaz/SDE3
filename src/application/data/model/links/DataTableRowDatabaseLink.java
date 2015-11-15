package application.data.model.links;

import application.data.model.DatabaseLink;
import application.node.objects.datatable.DataTableRow;

public class DataTableRowDatabaseLink extends DatabaseLink {
    public DataTableRowDatabaseLink() {
        super("data_table_rows", DataTableRow.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuidFromString", String.class)); // 1
        link("node_id", method("getParentId"), null); // 2
    }
}
