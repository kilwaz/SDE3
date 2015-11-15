package application.data.model.links;

import application.data.model.DatabaseLink;
import application.node.objects.datatable.DataTableValue;

public class DataTableValueDatabaseLink extends DatabaseLink {
    public DataTableValueDatabaseLink() {
        super("data_table_values", DataTableValue.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuidFromString", String.class)); // 1
        link("data_table_id", method("getParentId"), null); // 2
        link("data_key", method("getDataKey"), method("setDataKey", String.class)); // 3
        link("data_value", method("getDataValue"), method("setDataValue", String.class)); // 4
    }
}
