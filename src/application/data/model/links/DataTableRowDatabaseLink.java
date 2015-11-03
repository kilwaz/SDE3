package application.data.model.links;

import application.data.model.DatabaseLink;
import application.error.Error;
import application.node.objects.datatable.DataTableRow;

public class DataTableRowDatabaseLink extends DatabaseLink {
    public DataTableRowDatabaseLink() {
        super("data_table_rows");
        try {
            // Make sure the order is the same as column order in database
            link("node_id", DataTableRow.class.getMethod("getParentId")); // 1
        } catch (NoSuchMethodException ex) {
            Error.DATA_LINK_METHOD_NOT_FOUND.record().create(ex);
        }
    }
}
