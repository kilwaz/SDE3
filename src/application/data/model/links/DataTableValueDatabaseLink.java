package application.data.model.links;

import application.data.model.DatabaseLink;
import application.error.Error;
import application.node.objects.datatable.DataTableValue;

public class DataTableValueDatabaseLink extends DatabaseLink {
    public DataTableValueDatabaseLink() {
        super("data_table_values");
        try {
            // Make sure the order is the same as column order in database
            link("data_table_id", DataTableValue.class.getMethod("getParentId")); // 1
            link("data_key", DataTableValue.class.getMethod("getDataKey")); // 2
            link("data_value", DataTableValue.class.getMethod("getDataValue")); // 3
        } catch (NoSuchMethodException ex) {
            Error.DATA_LINK_METHOD_NOT_FOUND.record().create(ex);
        }
    }
}
